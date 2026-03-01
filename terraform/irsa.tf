data "aws_iam_policy_document" "video_workflow_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [local.oidc_provider_arn]
    }

    condition {
      test = "StringEquals"

      variable = format(
        "%s:sub",
        replace(local.oidc_provider_url, "https://", "")
      )

      values = [
        "system:serviceaccount:${kubernetes_namespace.hackathon.metadata[0].name}:${kubernetes_service_account.video_workflow_sa.metadata[0].name}"
      ]
    }
  }
}