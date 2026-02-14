kubectl apply -k k8s
kubectl delete -k k8s
kubectl get all -n minio-gateway

Notes:
- Pod-to-pod auth uses Keycloak Service DNS via `AUTH_INTERNAL_BASE_URL` in `k8s/deployment.yaml`.
- Browser redirects use `AUTH_EXTERNAL_ISSUER_URI` (ex: `http://auth.s4v3.local/realms/intwork`).
