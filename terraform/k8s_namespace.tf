resource "kubernetes_namespace" "hackathon" {
  metadata {
    name = "hackathon"
  }
}
