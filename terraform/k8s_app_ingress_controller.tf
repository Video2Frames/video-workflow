resource "kubernetes_ingress_v1" "app" {
  metadata {
    name      = "${local.app_name}-route"
    namespace = local.namespace

    annotations = {
      "nginx.ingress.kubernetes.io/rewrite-target" = "/$$1"
      "nginx.ingress.kubernetes.io/use-regex"      = "true"
    }
  }

  wait_for_load_balancer = true

  spec {
    ingress_class_name = "nginx"

    rule {
      http {
        path {
          path      = "/hackathon/v1/video-workflow/(.*)"
          path_type = "ImplementationSpecific"

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