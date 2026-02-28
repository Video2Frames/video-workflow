resource "kubernetes_deployment" "app" {
  metadata {
    name      = "video-workflow-deployment"
    namespace = kubernetes_namespace.hackathon.metadata[0].name
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "video-workflow-app"
      }
    }

    template {
      metadata {
        labels = {
          app = "video-workflow-app"
        }
      }

      spec {
        service_account_name = "video-workflow-sa"

        container {
          name              = "video-workflow-app"
          image             = var.app_image
          image_pull_policy = "Always"

          port {
            container_port = 8080
          }

          resources {
            limits = {
              cpu    = "1"
              memory = "1Gi"
            }
            requests = {
              cpu    = "500m"
              memory = "512Mi"
            }
          }

          env {
            name = "SPRING_DATASOURCE_URL"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "SPRING_DATASOURCE_URL"
              }
            }
          }

          env {
            name = "SPRING_DATASOURCE_USERNAME"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "SPRING_DATASOURCE_USERNAME"
              }
            }
          }

          env {
            name = "SPRING_DATASOURCE_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "SPRING_DATASOURCE_PASSWORD"
              }
            }
          }

          env {
            name = "AWS_SNS_TOPIC_ARN"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "AWS_SNS_TOPIC_ARN"
              }
            }
          }

          env {
            name = "AWS_SQS_STATUS_QUEUE_URL"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "AWS_SQS_STATUS_QUEUE_URL"
              }
            }
          }

          env {
            name = "VIDEO_BUCKET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app_secret.metadata[0].name
                key  = "VIDEO_BUCKET"
              }
            }
          }
        }
      }
    }
  }
}
