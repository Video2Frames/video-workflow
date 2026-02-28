package com.store.workflowService.update.domain.repository;

import com.store.workflowService.update.domain.model.VideoWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoWorkflowRepository extends JpaRepository<VideoWorkflow, UUID> {
    Optional<VideoWorkflow> findByVideoId(String videoId);

    // find all video workflows for a given user
    List<VideoWorkflow> findByUserId(String userId);
}
