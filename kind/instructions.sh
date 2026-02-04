#!/usr/bin/env sh
echo "Hello K8s"
kind --version
echo "Create cluster s4v3"
kind create cluster --name s4v3 --config kind-ingress.yaml
sleep 5
echo "apply kind ingress nginx"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
echo "wait for it..."
sleep 5
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s

echo "Apply the services: kubectl apply -k k8s/ for Keycloak, BE, then Gateway"
echo "Then apply the ingress: kubectl apply -f kind/minio-gateway-ingress.yaml"

