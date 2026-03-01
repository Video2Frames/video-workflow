locals {
  jdbc_url = "jdbc:postgresql://${data.aws_db_instance.db_instance.address}:${data.aws_db_instance.db_instance.port}/soat"

  # IRSA
  oidc_provider_arn = data.terraform_remote_state.infra.outputs.oidc_provider_arn
  oidc_provider_url = data.terraform_remote_state.infra.outputs.oidc_provider_url

  # Optional endpoints and S3 settings (fall back to empty or provided vars)
  aws_s3_endpoint   = var.aws_s3_endpoint
  aws_s3_path_style = var.aws_s3_path_style_access
}
