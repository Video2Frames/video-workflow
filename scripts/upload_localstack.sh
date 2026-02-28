#!/usr/bin/env bash
set -euo pipefail

# Upload helper for LocalStack S3
# Usage:
#   ./scripts/upload_localstack.sh <file> [s3-key] [bucket] [--disposition]
# Examples:
#   ./scripts/upload_localstack.sh download/my.zip
#   ./scripts/upload_localstack.sh download/my.zip folder/my.zip video-bucket --disposition

FILE=""
KEY=""
BUCKET="video-bucket"
SET_DISPOSITION=false

# Parse args: accept --disposition anywhere
while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --disposition)
      SET_DISPOSITION=true
      shift
      ;;
    --)
      shift
      break
      ;;
    -* )
      echo "Unknown option: $1" >&2
      exit 2
      ;;
    *)
      if [[ -z "$FILE" ]]; then
        FILE="$1"
      elif [[ -z "$KEY" ]]; then
        KEY="$1"
      elif [[ -z "$BUCKET" ]]; then
        BUCKET="$1"
      else
        echo "Extra argument ignored: $1" >&2
      fi
      shift
      ;;
  esac
done

if [[ -z "$FILE" ]]; then
  echo "Usage: $0 <file> [s3-key] [bucket] [--disposition]"
  exit 2
fi

if [[ ! -f "$FILE" ]]; then
  echo "File not found: $FILE"
  exit 3
fi

if [[ -z "$KEY" ]]; then
  KEY=$(basename "$FILE")
fi

# prefer awslocal if available
if command -v awslocal >/dev/null 2>&1; then
  S3_CMD="awslocal"
  UPLOAD_CMD=(s3 cp "$FILE" "s3://$BUCKET/$KEY" --content-type application/zip)
  PUT_CMD=(s3api put-object --bucket "$BUCKET" --key "$KEY" --body "$FILE" --content-type application/zip)
else
  # fallback to aws with endpoint override
  if command -v aws >/dev/null 2>&1; then
    S3_CMD="aws --endpoint-url=http://localhost:4566"
    UPLOAD_CMD=(s3 cp "$FILE" "s3://$BUCKET/$KEY" --content-type application/zip)
    PUT_CMD=(s3api put-object --bucket "$BUCKET" --key "$KEY" --body "$FILE" --content-type application/zip)
  else
    echo "Neither 'awslocal' nor 'aws' CLI found in PATH."
    echo "Install awslocal (pipx install awscli-local) or aws cli (brew install awscli)."
    exit 4
  fi
fi

echo "Using: $S3_CMD"
# perform upload
echo "Uploading $FILE -> s3://$BUCKET/$KEY"
if ! $S3_CMD "${UPLOAD_CMD[@]}"; then
  echo "Upload failed" >&2
  exit 5
fi

if [[ "$SET_DISPOSITION" == true ]]; then
  # set Content-Disposition via put-object (overwrites metadata)
  echo "Setting Content-Disposition metadata to force download filename=$KEY"
  if ! $S3_CMD "${PUT_CMD[@]}" --content-disposition "attachment; filename=\"$KEY\""; then
    echo "Failed to set content-disposition" >&2
    exit 6
  fi
fi

echo "Upload finished. Head object:"
$S3_CMD s3api head-object --bucket "$BUCKET" --key "$KEY" || true

exit 0

