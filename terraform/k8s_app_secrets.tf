resource "kubernetes_secret" "app_secret" {
  metadata {
    name      = "app-secret"
    namespace = kubernetes_namespace.hackathon.metadata[0].name
  }

  data = {
    SPRING_DATASOURCE_URL      = var.spring_datasource_url
    SPRING_DATASOURCE_USERNAME = var.db_user
    SPRING_DATASOURCE_PASSWORD = var.db_password

    AWS_REGION                 = var.region
    VIDEO_BUCKET               = var.video_bucket

    AWS_SNS_TOPIC_ARN          = var.sns_topic_arn
    AWS_SNS_ENDPOINT           = var.aws_sns_endpoint

    AWS_SQS_STATUS_QUEUE_URL   = var.aws_sqs_status_queue_url

    AWS_S3_ENDPOINT            = var.aws_s3_endpoint
    AWS_S3_PATH_STYLE_ACCESS   = tostring(var.aws_s3_path_style_access)
  }

  type = "Opaque"
}