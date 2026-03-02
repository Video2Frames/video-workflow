terraform {
  backend "s3" {
    bucket = "fiap-soat-hackathon-2026-tfstate"
    key    = "PRD/video-workflow/terraform.tfstate"
    region = "us-east-1"
  }
}