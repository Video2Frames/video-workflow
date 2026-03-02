variable "region" {}
variable "eks_cluster_name" {}

variable "app_image" {}

variable "db_user" {
  sensitive = true
}

variable "db_password" {
  sensitive = true
}

variable "video_bucket" {}
variable "aws_sqs_status_queue_url" {}
variable "sns_topic_arn" {}

variable "force_rollout" {
  default = ""
}

variable "aws_s3_endpoint" {
  type    = string
  default = ""
}

variable "aws_s3_path_style_access" {
  type    = string
  default = "false"
}

variable "spring_datasource_url" {}