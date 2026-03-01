resource "kubernetes_deployment" "app" {
  metadata {
    name      = "video-workflow-deployment"
    namespace = kubernetes_namespace.hackathon.metadata[0].name

    annotations = {
      force_rollout = var.force_rollout
    }
  }

  spec {
    replicas = 2

    selector {
      match_labels = {
        app = "video-workflow-app"
      }
    }

    strategy {
      type = "RollingUpdate"

      rolling_update {
        max_unavailable = 0
        max_surge       = 1
      }
    }

    template {
      metadata {
        labels = {
          app = "video-workflow-app"
        }
      }

      spec {
        service_account_name = kubernetes_service_account.video_workflow_sa.metadata[0].name
        termination_grace_period_seconds = 30

        container {
          name              = "video-workflow-app"
          image             = var.app_image
          image_pull_policy = "Always"

          port {
            container_port = 8080
            name           = "http"
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

          # ==============================
          # ENV FROM SECRET (mais limpo)
          # ==============================
          env_from {
            secret_ref {
              name = kubernetes_secret.app_secret.metadata[0].name
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

          # ==============================
          # HEALTH CHECKS
          # ==============================
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 20
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 3
          }

          liveness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 20
            timeout_seconds       = 5
            failure_threshold     = 5
          }
        }
      }
    }
  }
}