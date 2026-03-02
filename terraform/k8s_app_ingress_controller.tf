resource "kubernetes_ingress_v1" "app" {
  metadata {
    name      = "${local.app_name}-route"
    namespace = local.namespace
  }

  wait_for_load_balancer = true

  spec {
    ingress_class_name = "nginx"

    rule {
      http {
        path {
          path      = "/hackathon/v1/video-workflow"
          path_type = "Prefix"

          backend {
            service {
              name = kubernetes_service.app.metadata[0].name
              port {
                number = 80
              }
            }
          }
        }
      }
    }
  }
}