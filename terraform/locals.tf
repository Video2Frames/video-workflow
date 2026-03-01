locals {
  jdbc_url = "jdbc:postgresql://${data.aws_db_instance.db_instance.address}:${data.aws_db_instance.db_instance.port}/soat"

  sns_topic_arn     = data.terraform_remote_state.infra.outputs.order_created_topic_arn
  status_queue_url  = data.terraform_remote_state.infra.outputs.status_updates_queue_url
  status_queue_arn  = data.terraform_remote_state.infra.outputs.status_updates_queue_arn
  video_bucket_name = data.terraform_remote_state.infra.outputs.video_bucket_name

  # IRSA
  oidc_provider_arn = data.terraform_remote_state.infra.outputs.oidc_provider_arn
  oidc_provider_url = data.terraform_remote_state.infra.outputs.oidc_provider_url

  # Optional endpoints and S3 settings (fall back to empty or provided vars)
  aws_s3_endpoint       = var.aws_s3_endpoint
  aws_s3_path_style     = var.aws_s3_path_style_access
}
