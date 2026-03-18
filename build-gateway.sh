#!/usr/bin/env bash

# Use docker buildx for cross-compilation from ARM to AMD64
docker build --platform linux/amd64 -t gluonstream/minio-gateway:latest .

# Push the image to the registry
docker push gluonstream/minio-gateway:latest

# Optional: Load the image if using a local kind cluster
# kind load docker-image gluonstream/minio-gateway:latest --name blog.s4v3

# Restart the deployment to pick up the new image
kubectl rollout restart deployment.apps/minio-gateway -n horus-namespace
