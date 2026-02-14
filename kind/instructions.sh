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

echo "No hostAliases needed: use internal Service DNS for pod-to-pod and external hostnames for browser redirects."
echo "Internal auth URL: http://keycloak.keycloak-namespace.svc.cluster.local:8080 (set via AUTH_INTERNAL_BASE_URL)"
echo "External auth URL: http://auth.s4v3.local/realms/intwork (set via AUTH_EXTERNAL_ISSUER_URI)"
echo "Apply the services: kubectl apply -k k8s/ for Keycloak, BE, then Gateway"
echo "Then apply the ingress: kubectl apply -f k8s/minio-gateway-ingress.yaml"
echo "After building the image:"
echo "  docker push gluonstream/minio-gateway:latest     "
echo "to upgrade: kubectl rollout status deployment/minio-gateway -n minio-gateway --timeout=180s"
