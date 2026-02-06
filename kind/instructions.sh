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

echo " get the kubectl get nodes -o wide and note the IP, you will need it in deployment aliases"
echo "you need to put the ip in the deployment in hostAliases 172.21.0.2 in both: gateway, be-minio for now. This is basically an internal k8s DNS for pods"
kubectl get nodes -o wide
echo "Apply the services: kubectl apply -k k8s/ for Keycloak, BE, then Gateway"
echo "Then apply the ingress: kubectl apply -f k8s/minio-gateway-ingress.yaml"
echo "After building the image:"
echo "  docker push gluonstream/minio-gateway:latest     "
echo "to upgrade: kubectl rollout status deployment/minio-gateway -n minio-gateway --timeout=180s"