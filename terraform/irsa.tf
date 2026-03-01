#############################################
# IRSA - Assume Role Policy
#############################################

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
        "system:serviceaccount:hackathon:video-workflow-sa"
      ]
    }
  }
}

resource "aws_iam_role" "video_workflow_irsa" {
  name               = "video-workflow-irsa-role"
  assume_role_policy = data.aws_iam_policy_document.video_workflow_assume_role.json
}

resource "aws_iam_role_policy_attachment" "video_workflow_attach" {
  role       = aws_iam_role.video_workflow_irsa.name
  policy_arn = aws_iam_policy.video_workflow_policy.arn
}
