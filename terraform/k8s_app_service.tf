resource "kubernetes_service" "app" {
  metadata {
    name      = "${local.app_name}-service"
    namespace = local.namespace
  }

  spec {
    selector = local.labels

    port {
      port        = 80
      target_port = 8080
    }

    type = "ClusterIP"
  }
}