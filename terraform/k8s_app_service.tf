resource "kubernetes_service" "app" {
  metadata {
    name      = "video-workflow-service"
    namespace = kubernetes_namespace.hackathon.metadata[0].name
  }

  spec {
    type = "ClusterIP"

    selector = {
      app = "video-workflow-app"
    }

    port {
      port        = 80
      target_port = 8080
    }
  }
}
