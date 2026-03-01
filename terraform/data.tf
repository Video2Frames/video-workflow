data "aws_availability_zones" "available" {}

data "terraform_remote_state" "infra" {
  backend = "s3"
  config = {
    bucket = "fiap-soat-hackathon-2026-tfstate"
    key    = "PRD/hackathon-infra"
    region = "us-east-1"
  }
}

data "aws_eks_cluster" "cluster" {
  name = data.terraform_remote_state.infra.outputs.cluster_name
}

data "aws_eks_cluster_auth" "cluster" {
  name = data.aws_eks_cluster.cluster.name
}

data "terraform_remote_state" "database" {
  backend = "s3"
  config = {
    bucket = "fiap-soat-hackathon-2026-tfstate"
    key    = "PRD/hackathon-infra"
    region = "us-east-1"
  }
}

data "aws_sqs_queue" "status_queue" {
  name = replace(var.aws_sqs_status_queue_url, ".*/", "")
}

data "aws_db_instance" "db_instance" {
  db_instance_identifier = data.terraform_remote_state.database.outputs.database_identifier
}

data "aws_caller_identity" "current" {}
