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
        # Use static service account subject to avoid a dependency cycle with kubernetes_service_account
        "system:serviceaccount:hackathon:video-workflow-sa"
      ]
    }
  }
}

# Create the IAM role for IRSA and attach the policy defined in irsa_eks.tf
resource "aws_iam_role" "video_workflow_irsa" {
  name               = "video-workflow-irsa"
  assume_role_policy = data.aws_iam_policy_document.video_workflow_assume_role.json
}

resource "aws_iam_role_policy_attachment" "video_workflow_irsa_attach" {
  role       = aws_iam_role.video_workflow_irsa.name
  policy_arn = aws_iam_policy.video_workflow_policy.arn
}
