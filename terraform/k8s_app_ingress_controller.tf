resource "kubernetes_ingress_v1" "app" {
  metadata {
    name      = "video-workflow-route"
    namespace = kubernetes_namespace.hackathon.metadata[0].name

    annotations = {
      "nginx.ingress.kubernetes.io/backend-protocol"   = "HTTP"
      "nginx.ingress.kubernetes.io/proxy-read-timeout" = "60"
      "nginx.ingress.kubernetes.io/proxy-send-timeout" = "60"
    }
  }

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
