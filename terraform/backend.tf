terraform {
  required_version = ">= 1.5.0"

  backend "s3" {
    bucket         = "fiap-soat-hackathon-2026-tfstate"
    key            = "apps/video-workflow/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}