resource "kubernetes_service_account" "video_workflow_sa" {
  metadata {
    name      = "video-workflow-sa"
    namespace = kubernetes_namespace.hackathon.metadata[0].name

    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.video_workflow_irsa.arn
    }
  }
}
