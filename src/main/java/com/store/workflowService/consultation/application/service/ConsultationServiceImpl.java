package com.store.workflowService.consultation.application.service;

import com.store.workflowService.consultation.application.usecases.ConsultationUseCase;
import com.store.workflowService.consultation.application.usecases.dto.DownloadResult;
import com.store.workflowService.consultation.application.usecases.dto.VideoDto;
import com.store.workflowService.update.domain.model.VideoWorkflow;
import com.store.workflowService.update.domain.repository.VideoWorkflowRepository;
import com.store.workflowService.upload.infra.config.UploadProperties;
import com.store.workflowService.utils.exception.VideoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationServiceImpl implements ConsultationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultationServiceImpl.class);

    private final VideoWorkflowRepository repository;
    private final S3Client s3Client;
    private final UploadProperties uploadProperties;

    public ConsultationServiceImpl(VideoWorkflowRepository repository, S3Client s3Client, UploadProperties uploadProperties) {
        this.repository = repository;
        this.s3Client = s3Client;
        this.uploadProperties = uploadProperties;
    }

    @Override
    public List<VideoDto> listByUser(String userId) {
        log.info("Listing videos for user={}", userId);
        List<VideoWorkflow> list = repository.findByUserId(userId);
        return list.stream().map(v -> new VideoDto(
                v.getVideoId(),
                v.getUserId(),
                v.getUploadPath(),
                v.getOutputPath(),
                v.getStatus(),
                v.getUploadedAt(),
                v.getProcessedAt()
        )).collect(Collectors.toList());
    }

    @Override
    public DownloadResult downloadOutput(String userId, String videoId) {
        log.info("Download requested user={} videoId={}", userId, videoId);
        VideoWorkflow wf = repository.findByVideoId(videoId).orElse(null);
        if (wf == null) {
            throw new VideoNotFoundException("Video not found: " + videoId);
        }
        if (!userId.equals(wf.getUserId())) {
            // Avoid leaking ownership info; treat as not found
            throw new VideoNotFoundException("Video not found: " + videoId);
        }
        String outputPath = wf.getOutputPath();
        if (outputPath == null || outputPath.isBlank()) {
            throw new VideoNotFoundException("Video has no output path: " + videoId);
        }

        String bucket = uploadProperties.getBucket();
        String key;


        if (outputPath.startsWith("s3://")) {
            String withoutPrefix = outputPath.substring(5);
            int slash = withoutPrefix.indexOf('/');
            if (slash > 0) {
                bucket = withoutPrefix.substring(0, slash);
                key = withoutPrefix.substring(slash + 1);
            } else {
                key = "";
            }
        } else if (outputPath.contains("/")) {
            key = outputPath.startsWith("/") ? outputPath.substring(1) : outputPath;
        } else {
            key = uploadProperties.getKeyPrefix()
                    + "/"
                    + wf.getUserId()
                    + "/"
                    + wf.getVideoId()
                    + "-"
                    + outputPath;
        }

        // Normalize key: remove leading slash and accidental bucket prefix
        if (key != null && !key.isBlank()) {
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            String maybeBucketPrefix = bucket + "/";
            if (key.startsWith(maybeBucketPrefix)) {
                key = key.substring(maybeBucketPrefix.length());
            }
        }

        log.info("Resolved S3 target for videoId={} originalOutputPath={} -> bucket={} key={}", videoId, outputPath, bucket, key);

        if (key == null || key.isBlank()) {
            log.warn("Computed S3 key is empty for videoId={} outputPath={}", videoId, outputPath);
            throw new RuntimeException("Invalid S3 key for video: " + videoId);
        }

        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            // Try single object first
            InputStream is = s3Client.getObject(req);
            InputStreamResource resource = new InputStreamResource(is);
            String fileName = key;
            int idx = key.lastIndexOf('/');
            if (idx >= 0 && idx + 1 < key.length()) {
                fileName = key.substring(idx + 1);
            }
            return new DownloadResult(resource, MediaType.APPLICATION_OCTET_STREAM, fileName);
        } catch (NoSuchKeyException e) {
            log.info("S3 object not found bucket={} key={} for videoId={} (attempting prefix list)", bucket, key, videoId);
            // Try listing objects under the key as a prefix (treat as folder)
            String prefix = key.endsWith("/") ? key : key + "/";
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();
            ListObjectsV2Response listRes = s3Client.listObjectsV2(listReq);
            List<S3Object> contents = listRes.contents();
            if (contents == null || contents.isEmpty()) {
                log.info("No objects found under prefix bucket={} prefix={} for videoId={}", bucket, prefix, videoId);
                throw new VideoNotFoundException("S3 object not found: " + bucket + "/" + key);
            }

            try {
                // Create piped streams to stream a ZIP on-the-fly
                final PipedInputStream pis = new PipedInputStream(32 * 1024);
                final PipedOutputStream pos = new PipedOutputStream(pis);

                InputStreamResource resource = new InputStreamResource(pis);

                // Determine a zip filename (use last segment of key or videoId)
                String zipBase = key;
                int idx = zipBase.lastIndexOf('/');
                if (idx >= 0 && idx + 1 < zipBase.length()) {
                    zipBase = zipBase.substring(idx + 1);
                }
                if (zipBase.isBlank()) {
                    zipBase = wf.getVideoId();
                }
                final String zipFileName = zipBase + ".zip";

                // Make final copies for variables accessed from the lambda
                final List<S3Object> contentsForZip = contents;
                final String prefixForZip = prefix;
                final String bucketForZip = bucket;
                final S3Client s3ClientForZip = this.s3Client;
                final String videoIdForZip = videoId;

                java.util.concurrent.atomic.AtomicBoolean writerStarted = new java.util.concurrent.atomic.AtomicBoolean(false);
                try {
                    Thread writer = new Thread(() -> {
                        try (ZipOutputStream zos = new ZipOutputStream(pos)) {
                            for (S3Object obj : contentsForZip) {
                                String objKey = obj.key();
                                String entryName = objKey.substring(prefixForZip.length());
                                if (entryName.isEmpty()) {
                                    continue;
                                }
                                try {
                                    zos.putNextEntry(new ZipEntry(entryName));
                                    try (InputStream objIs = s3ClientForZip.getObject(GetObjectRequest.builder().bucket(bucketForZip).key(objKey).build())) {
                                        copy(objIs, zos);
                                    }
                                    zos.closeEntry();
                                } catch (Exception ex) {
                                    log.error("Error adding S3 object to zip bucket={} key={} entry={} : {}", bucketForZip, objKey, entryName, ex.toString());
                                }
                            }
                            zos.finish();
                        } catch (IOException ioe) {
                            log.error("I/O error streaming ZIP for videoId={}: {}", videoIdForZip, ioe.toString());
                            try { pos.close(); } catch (Exception ignore) {}
                        } finally {
                            try { pos.close(); } catch (Exception ignore) {}
                        }
                    }, "s3-zip-writer-" + videoIdForZip);
                    writer.setDaemon(true);
                    writer.start();
                    writerStarted.set(true);

                    return new DownloadResult(resource, MediaType.APPLICATION_OCTET_STREAM, zipFileName);
                } catch (Exception ex) {
                    // If anything failed before the writer started, ensure streams are closed to avoid leaks
                    try { pos.close(); } catch (Exception ignore) {}
                    try { pis.close(); } catch (Exception ignore) {}
                    throw new RuntimeException("Error preparing zip stream", ex);
                } finally {
                    if (!writerStarted.get()) {
                        try { pos.close(); } catch (Exception ignore) {}
                    }
                }
            } catch (IOException io) {
                log.error("Error preparing zip stream for videoId={}: {}", videoId, io.toString());
                throw new RuntimeException("Error preparing zip stream", io);
            }

        } catch (SdkServiceException e) {
            log.error("S3 access error while fetching bucket={} key={} for videoId={} (originalOutputPath={}): {}", bucket, key, videoId, outputPath, e.toString());
            throw new RuntimeException("Error accessing S3 object: " + e.getMessage(), e);
        }
    }

    // Utility copy method
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
    }
}
