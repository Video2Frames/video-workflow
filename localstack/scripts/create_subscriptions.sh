#!/usr/bin/env bash
set -euo pipefail

# Script helper para (re)criar subscriptions SNS -> SQS dentro do container localstack
# Usa docker exec localstack awslocal ... para garantir execução mesmo sem awslocal instalado localmente

TOPIC_ARN="arn:aws:sns:us-east-1:000000000000:video-processing-topic.fifo"

subscribe_queue() {
  QUEUE_NAME="$1"
  FILTER_JSON="$2"

  echo "Processing queue: $QUEUE_NAME"

  QUEUE_URL=$(docker exec localstack awslocal sqs get-queue-url --queue-name "$QUEUE_NAME" --output text)
  if [ -z "$QUEUE_URL" ]; then
    echo "ERROR: could not get queue-url for $QUEUE_NAME"
    return 1
  fi
  echo "  Queue URL: $QUEUE_URL"

  QUEUE_ARN=$(docker exec localstack awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)
  echo "  Queue ARN: $QUEUE_ARN"

  echo "  Creating subscription..."
  SUB_ARN=$(docker exec localstack awslocal sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN" --return-subscription-arn --output text)
  echo "  Subscription ARN: $SUB_ARN"

  if [ -n "$FILTER_JSON" ]; then
    echo "  Setting FilterPolicy: $FILTER_JSON"
    docker exec localstack awslocal sns set-subscription-attributes --subscription-arn "$SUB_ARN" --attribute-name FilterPolicy --attribute-value "$FILTER_JSON"
  fi

  # Apply a basic queue policy to allow the topic to send messages
  echo "  Applying queue policy to allow SNS topic to send messages"
  QUEUE_POLICY=$(cat <<EOF
{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":"*","Action":"sqs:SendMessage","Resource":"$QUEUE_ARN","Condition":{"ArnEquals":{"aws:SourceArn":"$TOPIC_ARN"}}}]}
EOF
)
  docker exec localstack awslocal sqs set-queue-attributes --queue-url "$QUEUE_URL" --attributes "Policy=$QUEUE_POLICY"

  echo "  Done for $QUEUE_NAME"
}

# Create subscriptions
subscribe_queue "video-processing-queue.fifo" '{"event_type":["video.uploaded"]}'
subscribe_queue "register-processing-queue.fifo" '{"event_type":["user.register"]}'

echo "\nResulting subscriptions for topic $TOPIC_ARN:"
docker exec localstack awslocal sns list-subscriptions-by-topic --topic-arn "$TOPIC_ARN" --output json || true

echo "\nDone."

