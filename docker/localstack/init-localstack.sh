#!/bin/bash
 set -e

 SNS_TOPIC_NAME="sns-ms-shopping-cart"

 # Cria SNS topic
 TOPIC_ARN=$(aws --endpoint-url=http://localstack:4566 sns create-topic --name $SNS_TOPIC_NAME --query 'TopicArn' --output text)
 echo "SNS criado: $TOPIC_ARN"

 # Cria SQS queue
 QUEUE_URL=$(aws --endpoint-url=http://localstack:4566 sqs create-queue --queue-name minha-fila --query 'QueueUrl' --output text)
 echo "SQS criado: $QUEUE_URL"

 # Pega ARN da SQS
 QUEUE_ARN=$(aws --endpoint-url=http://localstack:4566 sqs get-queue-attributes --queue-url $QUEUE_URL --attribute-name QueueArn --query "Attributes.QueueArn" --output text)

 # Cria subscription da SQS no SNS
 SUBSCRIPTION_ARN=$(aws --endpoint-url=http://localstack:4566 sns subscribe --topic-arn $TOPIC_ARN --protocol sqs --notification-endpoint $QUEUE_ARN --query 'SubscriptionArn' --output text)
 echo "Subscription criada: $SUBSCRIPTION_ARN"

 # Exporta variáveis para o ambiente do script
 export SNS_TOPIC_NAME=$SNS_TOPIC_NAME
 export SNS_TOPIC_ARN=$TOPIC_ARN