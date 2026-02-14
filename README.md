# Keycloak Realm Import

TL;DR
```bash
kubectl apply -k ./k8s
```
### How it works
Docker exposes it on http://localhost:8001
All *.json files in ./keycloak/ are imported once
Realm import happens:
 only at startup
 only if the realm does not already exist
 only with --import-realm

## Access URLs

- Keycloak: http://localhost:8001
- Admin Console: http://localhost:8001/admin
- Health Check: http://localhost:8001/health/live

## Dev vs Prod Notes

#### Dev:
- Use realm JSON
- Include users & secrets
- Optionally remove the data volume for clean re-imports

#### Prod:
- Import once
- Remove users/secrets from JSON
- Manage changes via Admin API / Terraform / Ansible / Kubernetes Helm charts
- Harden this configuration (HTTPS, strict hostnames, reverse proxy)

### Best practices

- Use JSON imports for fast, deterministic bootstrap in local development and CI:
- Import a realm JSON that includes:
  - Test users
  - Client secrets (only for dev)
  - Default roles and clients
  - Keep imports simple so developers can spin up Keycloak quickly.
  - Place your export in Keycloak's import location (or mount it) and start Keycloak with --import-realm for the initial run.
- Avoid relying on JSON imports for ongoing configuration in production:
  - Use realm JSON only for the initial bootstrap (one-time).
  - Remove sensitive data from the JSON:
  - Users and passwords
  - Client secrets

### Manage configuration changes using:
Keycloak Admin REST API
Terraform Keycloak provider
Ansible or other automation tools
This prevents accidental overwrites and keeps secrets out of version control.
Common pitfalls to avoid
 - Misconfigured redirect URIs (wrong external port)
 - Attempting to re-import a realm after data already exists
 - Mixing manual Admin Console edits with JSON-based imports
 - Storing production secrets (passwords, client secrets) in the realm JSON

### Summary
 - Realm import is one-time only and intended for initial bootstrap.
 - Use JSON imports for dev and CI.
 - Use APIs or IaC tools for production and repeatable changes.
