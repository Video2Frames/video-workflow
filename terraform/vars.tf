variable "region" {
  description = "The AWS region to deploy resources in"
  default     = "us-east-1"
}

variable "force_rollout" {
  description = "A dummy variable to force redeployment of resources"
  type        = string
  default     = ""
}

variable "app_image" {
  description = "The Docker image for the application"
}

variable "db_user" {
  description = "The username for the RDS instance"
  sensitive   = true
}

variable "db_password" {
  description = "The password for the RDS instance"
  sensitive   = true
}

variable "sns_topic_arn" {
  description = "The ARN of the SNS topic for notifications"
}


variable "aws_s3_endpoint" {
  description = "Optional custom S3 endpoint (for localstack or custom S3-compatible endpoints)"
  type        = string
  default     = ""
}

variable "aws_sns_endpoint" {
  description = "Optional SNS endpoint override (used mainly for localstack/testing)"
  type        = string
  default     = ""
}

variable "aws_s3_path_style_access" {
  description = "Whether to use path style access for S3 (true/false)"
  type        = bool
  default     = false
}

variable "tags" {
  description = "A map of tags to assign to resources"
  default = {
    Environment = "PRD"
    Project     = "tc-app"
  }
}
variable "spring_datasource_url" {
  description = "The JDBC URL for the application's datasource"

}
variable "video_bucket" {
  description = "The name of the S3 bucket for storing videos"
}
variable "aws_sqs_status_queue_url" {
  description = "The URL of the SQS queue for status updates"
}
