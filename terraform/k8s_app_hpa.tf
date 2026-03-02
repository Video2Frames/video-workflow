resource "kubernetes_horizontal_pod_autoscaler_v2" "app" {
  metadata {
    name      = "${local.app_name}-hpa"
    namespace = local.namespace
  }

  spec {
    scale_target_ref {
      kind = "Deployment"
      name = kubernetes_deployment.app.metadata[0].name
    }

    min_replicas = 1
    max_replicas = 3
  }
}