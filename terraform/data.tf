data "terraform_remote_state" "infra" {
  backend = "s3"

  config = {
    bucket = "fiap-soat-hackathon-2026-tfstate"
    key    = "PRD/hackathon-infra"
    region = "us-east-1"
  }
}

data "aws_db_instance" "db_instance" {
  db_instance_identifier = data.terraform_remote_state.infra.outputs.database_identifier
}

data "aws_sqs_queue" "status_queue" {
  name = "status-updates.fifo"
}