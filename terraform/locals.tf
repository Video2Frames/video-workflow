locals {
  app_name  = "video-workflow"
  namespace = "hackathon"

  labels = {
    app = local.app_name
  }


    jdbc_url = "jdbc:postgresql://${data.aws_db_instance.db_instance.address}:5432/soat"
  }