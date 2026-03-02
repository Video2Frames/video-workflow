resource "kubernetes_namespace" "app" {
  metadata {
    name = local.namespace
  }
}