resource "aws_iam_policy" "video_workflow_policy" {
  name = "video-workflow-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [

      # SNS Publish
      {
        Effect = "Allow",
        Action = [
          "sns:Publish"
        ],
        Resource = var.sns_topic_arn
      },

      # SQS Consume
      {
        Effect = "Allow",
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ],
        Resource = data.aws_sqs_queue.status_queue.arn
      },

      # S3 Access
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = "arn:aws:s3:::${var.video_bucket}/*"
      }
    ]
  })
}