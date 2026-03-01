resource "kubernetes_secret" "app_secret" {
  metadata {
    name      = "video-workflow-secret"
    namespace = kubernetes_namespace.hackathon.metadata[0].name
  }

  type = "Opaque"

  data = {
    SPRING_DATASOURCE_URL      = local.jdbc_url
    SPRING_DATASOURCE_USERNAME = var.db_user
    SPRING_DATASOURCE_PASSWORD = var.db_password

    AWS_SNS_TOPIC_ARN        = local.sns_topic_arn
    AWS_SQS_STATUS_QUEUE_URL = local.status_queue_url
    VIDEO_BUCKET             = local.video_bucket_name
  }
}
