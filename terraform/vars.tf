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

variable "api_url" {
  description = "Base URL exposed by API Gateway / Ingress"
}

variable "aws_account_id" {
  description = "The AWS account ID"
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to resources"
  default = {
    Environment = "PRD"
    Project     = "tc-app"
  }
}
