resource "kubernetes_secret" "app" {
  metadata {
    name      = "app-secret"
    namespace = local.namespace
  }

  data = {
    SPRING_DATASOURCE_URL      = local.jdbc_url
    SPRING_DATASOURCE_USERNAME = var.db_user
    SPRING_DATASOURCE_PASSWORD = var.db_password

    VIDEO_BUCKET                    = var.video_bucket
    AWS_REGION                      = var.region
    AWS_SNS_TOPIC_ARN               = var.sns_topic_arn
    AWS_SQS_STATUS_QUEUE_URL        = var.aws_sqs_status_queue_url
    AWS_SQS_NOTIFICATIONS_QUEUE_URL = var.aws_sqs_notifications_queue_url

    AWS_S3_ENDPOINT          = var.aws_s3_endpoint
    AWS_S3_PATH_STYLE_ACCESS = var.aws_s3_path_style_access

    SPRING_JPA_HIBERNATE_DDL_AUTO = "create"
    SWAGGER_ENABLED               = "true"

    SES_FROM_EMAIL = var.ses_from_email
  }

  type = "Opaque"
}