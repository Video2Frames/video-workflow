data "aws_iam_policy_document" "video_workflow_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.terraform_remote_state.infra.outputs.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${data.terraform_remote_state.infra.outputs.oidc_provider_url}:sub"

      values = [
        "system:serviceaccount:hackathon:video-workflow-sa"
      ]
    }
  }
}



resource "aws_iam_role" "video_workflow_irsa" {
  name = "video-workflow-irsa"

  assume_role_policy = data.aws_iam_policy_document.video_workflow_assume_role.json
}