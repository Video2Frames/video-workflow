FROM localstack/localstack:latest

USER root

RUN apt-get update && \
    apt-get install -y jq && \
    apt-get clean

USER localstack
