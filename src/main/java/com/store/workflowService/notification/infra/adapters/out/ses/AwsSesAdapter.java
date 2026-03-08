package com.store.workflowService.notification.infra.adapters.out.ses;

import com.store.workflowService.update.app.usecases.dto.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

@Component
public class AwsSesAdapter {

    private static final Logger log = LoggerFactory.getLogger(AwsSesAdapter.class);

    private final SesClient sesClient;
    private final String fromEmail;

    public AwsSesAdapter(SesClient sesClient, @Value("${aws.ses.from-email}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    public void sendProcessedEmail(String to, StatusEvent event) {
        String subject = "seu video ja foi processado!";
        String bodyText = String.format("Sua pasta zip ja esta disponivel, utilize o id: %s para baixar a pasta!", event.getVideoId());
        sendEmail(to, subject, bodyText);
    }

    public void sendFailedEmail(String to, StatusEvent event) {
        String subject = "Houve falha no seu processamento!";
        String bodyText = String.format("O id do seu video é: %s entre em contato com a equipe tecnica e saiba o que aconteceu!", event.getVideoId());
        sendEmail(to, subject, bodyText);
    }

    private void sendEmail(String to, String subject, String bodyText) {
        try {
            Destination destination = Destination.builder().toAddresses(to).build();

            Content subj = Content.builder().data(subject).charset("UTF-8").build();
            Content bodyContent = Content.builder().data(bodyText).charset("UTF-8").build();
            Body body = Body.builder().text(bodyContent).build();
            Message message = Message.builder().subject(subj).body(body).build();

            SendEmailRequest req = SendEmailRequest.builder()
                    .destination(destination)
                    .message(message)
                    .source(fromEmail)
                    .build();

            SendEmailResponse resp = sesClient.sendEmail(req);
            log.info("SES sendEmail messageId={}", resp.messageId());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}

