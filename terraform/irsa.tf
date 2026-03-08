############################################
# OIDC Provider (busca o provider já criado pelo EKS)
############################################

data "aws_iam_openid_connect_provider" "oidc" {
  url = data.aws_eks_cluster.cluster.identity[0].oidc[0].issuer
}

############################################
# Assume Role Policy (IRSA)
############################################

data "aws_iam_policy_document" "assume_role" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type = "Federated"

      identifiers = [
        data.aws_iam_openid_connect_provider.oidc.arn
      ]
    }

    condition {
      test = "StringEquals"

      variable = "${replace(
        data.aws_eks_cluster.cluster.identity[0].oidc[0].issuer,
        "https://",
        ""
      )}:sub"

      values = [
        "system:serviceaccount:${local.namespace}:${local.app_name}"
      ]
    }
  }
}

############################################
# IAM Role para IRSA
############################################

resource "aws_iam_role" "irsa" {
  name               = "${local.app_name}-irsa"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

############################################
# Policy da aplicação
############################################

resource "aws_iam_policy" "video_workflow_policy" {
  name = "video-workflow-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      ####################################
      # Permissão para publicar no SNS
      ####################################
      {
        Effect   = "Allow"
        Action   = ["sns:Publish"]
        Resource = var.sns_topic_arn
      },

      ####################################
      # Permissão para consumir SQS (filas específicas)
      ####################################
      {
        Effect = "Allow"
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = [
          data.aws_sqs_queue.status_queue.arn,
          data.aws_sqs_queue.notifications_queue.arn
        ]
      },

      ####################################
      # Permissão para enviar email (SES)
      ####################################
      {
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail"
        ]
        Resource = "*"
      },

      ####################################
      # Bucket de upload de vídeos
      ####################################
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ]
        Resource = "arn:aws:s3:::${var.video_bucket}/*"
      },

      ####################################
      # Bucket de frames / zip gerado
      ####################################
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject"
        ]
        Resource = "arn:aws:s3:::video2frames-extracted-frames/*"
      }

    ]
  })
}

############################################
# Anexar a política à função IRSA
############################################

resource "aws_iam_role_policy_attachment" "video_workflow_attach" {
  role       = aws_iam_role.irsa.name
  policy_arn = aws_iam_policy.video_workflow_policy.arn
}

############################################
# Kubernetes Service Account com IRSA
############################################

resource "kubernetes_service_account" "app" {
  metadata {
    name      = local.app_name
    namespace = local.namespace

    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.irsa.arn
    }
  }
}