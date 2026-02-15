# Apply Local configuration
kubectl apply -k k8s/overlays/local

# Apply Net configuration
kubectl apply -k k8s/overlays/net

# Delete configuration (example for local)
kubectl delete -k k8s/overlays/local

kubectl get all -n horus-namespace
kubectl rollout restart deployment/minio-gateway -n horus-namespace
To wait for rollout:
kubectl rollout status deployment/minio-gateway -n horus-namespace

Notes:
- Configuration is split into `base` and `overlays`.
- Use `k8s/overlays/local` for local development.
- Use `k8s/overlays/net` for network/cloud deployment.
- Pod-to-pod auth uses Keycloak Service DNS via `AUTH_INTERNAL_BASE_URL` in the respective ConfigMap.
- Browser redirects use `AUTH_EXTERNAL_ISSUER_URI`.
