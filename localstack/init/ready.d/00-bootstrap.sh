#!/bin/bash
set -e

echo "===================================================="
echo "🚀 STARTING LOCALSTACK BOOTSTRAP"
echo "===================================================="

RESOURCE_DIR="/etc/localstack/resources"
AWS_REGION="${AWS_DEFAULT_REGION:-us-east-1}"
ACCOUNT_ID="000000000000"

log_section() {
  echo ""
  echo "----------------------------------------------------"
  echo "$1"
  echo "----------------------------------------------------"
}

# =========================
# S3
# =========================
log_section "📦 Criando Buckets S3..."

for file in "$RESOURCE_DIR"/s3/*.json; do
  [ -e "$file" ] || continue
  BUCKET_NAME=$(jq -r '.bucketName' "$file")

  echo "➡️  Criando bucket: $BUCKET_NAME"
  awslocal s3 mb "s3://$BUCKET_NAME" || true
  echo "✅ Bucket criado: $BUCKET_NAME"
done

echo ""
echo "📋 Buckets existentes:"
awslocal s3 ls

# =========================
# SNS
# =========================
log_section "📣 Criando SNS Topics..."

for file in "$RESOURCE_DIR"/sns/*.json; do
  [ -e "$file" ] || continue
  TOPIC_NAME=$(jq -r '.topicName' "$file")

  HAS_ATTRS=$(jq 'has("attributes")' "$file")
  if [ "$HAS_ATTRS" = "true" ]; then
    ATTR_KV=$(jq -r '.attributes | to_entries | map("\(.key)=\(.value)") | join(",")' "$file")
    echo "➡️  Criando SNS (com atributos): $TOPIC_NAME -> $ATTR_KV"
    awslocal sns create-topic \
      --name "$TOPIC_NAME" \
      --attributes "$ATTR_KV" > /dev/null || true
  else
    echo "➡️  Criando SNS: $TOPIC_NAME"
    awslocal sns create-topic --name "$TOPIC_NAME" > /dev/null || true
  fi

  echo "✅ SNS criado com sucesso: $TOPIC_NAME"
done

echo ""
echo "📋 SNS existentes:"
awslocal sns list-topics

# =========================
# SQS
# =========================
log_section "📬 Criando SQS Queues..."

for file in "$RESOURCE_DIR"/sqs/*.json; do
  [ -e "$file" ] || continue
  QUEUE_NAME=$(jq -r '.queueName' "$file")

  if [ -z "$QUEUE_NAME" ] || [ "$QUEUE_NAME" = "null" ]; then
    continue
  fi

  HAS_ATTRS=$(jq 'has("attributes")' "$file")
  if [ "$HAS_ATTRS" = "true" ]; then
    ATTR_JSON=$(jq -c '.attributes' "$file")
    echo "➡️  Criando SQS (com atributos): $QUEUE_NAME"
    awslocal sqs create-queue \
      --queue-name "$QUEUE_NAME" \
      --attributes "$ATTR_JSON" > /dev/null || true
  else
    awslocal sqs create-queue --queue-name "$QUEUE_NAME" > /dev/null || true
  fi

  echo "✅ SQS criada com sucesso: $QUEUE_NAME"
done

echo ""
echo "📋 Filas SQS existentes:"
awslocal sqs list-queues

# =========================
# SNS -> SQS Subscriptions
# =========================
log_section "🔗 Criando subscriptions SNS -> SQS..."

for file in "$RESOURCE_DIR"/sns/*.json; do
  [ -e "$file" ] || continue

  TOPIC_NAME=$(jq -r '.topicName' "$file")
  TOPIC_ARN="arn:aws:sns:${AWS_REGION}:${ACCOUNT_ID}:${TOPIC_NAME}"

  HAS_SUBS=$(jq 'has("subscriptions")' "$file")
  if [ "$HAS_SUBS" != "true" ]; then
    continue
  fi

  SUB_COUNT=$(jq '.subscriptions | length' "$file")
  if [ "$SUB_COUNT" -eq 0 ]; then
    continue
  fi

  echo "➡️  Processando subscriptions para: $TOPIC_NAME"

  for i in $(seq 0 $((SUB_COUNT - 1))); do
    QUEUE_NAME=$(jq -r ".subscriptions[$i].queueName" "$file")
    PROTOCOL=$(jq -r ".subscriptions[$i].protocol // \"sqs\"" "$file")

    QUEUE_URL=$(awslocal sqs get-queue-url \
      --queue-name "$QUEUE_NAME" \
      --query 'QueueUrl' \
      --output text)

    QUEUE_ARN=$(awslocal sqs get-queue-attributes \
      --queue-url "$QUEUE_URL" \
      --attribute-names QueueArn \
      --query 'Attributes.QueueArn' \
      --output text)

    echo "   ↳ Criando subscription: $QUEUE_NAME"

    SUB_ARN=$(awslocal sns subscribe \
      --topic-arn "$TOPIC_ARN" \
      --protocol "$PROTOCOL" \
      --notification-endpoint "$QUEUE_ARN" \
      --query SubscriptionArn \
      --output text)

    echo "   ✅ Subscription criada"

    # ================= FILTER POLICY =================
    HAS_FILTER=$(jq ".subscriptions[$i] | has(\"filterPolicy\")" "$file")
    if [ "$HAS_FILTER" = "true" ]; then
      FILTER_JSON=$(jq -c ".subscriptions[$i].filterPolicy" "$file")

      awslocal sns set-subscription-attributes \
        --subscription-arn "$SUB_ARN" \
        --attribute-name FilterPolicy \
        --attribute-value "$FILTER_JSON" > /dev/null

      echo "   ✅ FilterPolicy aplicada"
    fi

    # ================= QUEUE POLICY =================
    QUEUE_POLICY=$(jq -n \
      --arg resource "$QUEUE_ARN" \
      --arg topic "$TOPIC_ARN" \
      '{
        Version: "2012-10-17",
        Statement: [{
          Effect: "Allow",
          Principal: "*",
          Action: "sqs:SendMessage",
          Resource: $resource,
          Condition: {
            ArnEquals: { "aws:SourceArn": $topic }
          }
        }]
      }')

    # Converte o JSON da policy para string escapada
    QUEUE_POLICY_ESCAPED=$(echo "$QUEUE_POLICY" | jq -c . | jq -Rs .)

    awslocal sqs set-queue-attributes \
      --queue-url "$QUEUE_URL" \
      --attributes "{\"Policy\":$QUEUE_POLICY_ESCAPED}" > /dev/null

    echo "   ✅ Policy aplicada na fila"
  done
done

# =========================
# DynamoDB
# =========================
log_section "🗃 Criando Tabelas DynamoDB..."

for file in "$RESOURCE_DIR"/db/dynamo/*.json; do
  [ -e "$file" ] || continue
  TABLE_NAME=$(jq -r '.TableName' "$file")

  echo "➡️  Criando tabela DynamoDB: $TABLE_NAME"
  awslocal dynamodb create-table --cli-input-json file://"$file" > /dev/null || true
  echo "✅ Tabela criada com sucesso: $TABLE_NAME"
done

echo ""
echo "📋 Tabelas DynamoDB existentes:"
awslocal dynamodb list-tables

echo ""
echo "===================================================="
echo "🎉 LOCALSTACK BOOTSTRAP FINALIZADO COM SUCESSO"
echo "===================================================="
