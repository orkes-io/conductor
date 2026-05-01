# Deploying Conductor on Kubernetes

This guide covers deploying Conductor OSS on a Kubernetes cluster using plain manifests. An OpenShift-specific section at the bottom covers the small number of differences for that platform.

The stack is intentionally minimal: **Conductor server + PostgreSQL only.** No Elasticsearch and no Redis are required — Conductor uses PostgreSQL for both workflow persistence and task queuing when `conductor.db.type=postgres` is set.

## Prerequisites

- A Kubernetes cluster (1.25+) with permission to create Namespaces, Deployments, StatefulSets, Services, ConfigMaps, Secrets, and PersistentVolumeClaims
- `kubectl` configured to talk to your cluster
- A default StorageClass that can provision `ReadWriteOnce` volumes (true of most managed clusters and `kind`)
- PostgreSQL 14+ reachable from the cluster, **or** use the in-cluster StatefulSet in the manifest below

## Quick start

The file `deploy/kubernetes/conductor.yaml` in this repository contains everything needed to run a single-node Conductor deployment with an in-cluster PostgreSQL instance. It was tested against a `kind` cluster but works on any standard Kubernetes distribution.

```shell
kubectl apply -f deploy/kubernetes/conductor.yaml
```

Wait for both pods to reach `Running`:

```shell
kubectl get pods -n conductor -w
```

Once ready, verify the server is healthy:

```shell
kubectl port-forward -n conductor svc/conductor-server 8080:8080 5000:5000
curl http://localhost:8080/health
# {"healthy":true,...}
```

The Conductor UI is available at [http://localhost:5000](http://localhost:5000).

## What the manifest creates

| Resource | Name | Purpose |
|---|---|---|
| Namespace | `conductor` | Isolates all resources |
| Secret | `conductor-postgres-secret` | PostgreSQL credentials |
| ConfigMap | `conductor-config` | Server `.properties` file |
| StatefulSet | `conductor-postgres` | PostgreSQL 16 with 10Gi PVC |
| Service | `conductor-postgres` | Internal DNS for the DB |
| Deployment | `conductor-server` | Conductor server + UI (combined image) |
| Service | `conductor-server` | Exposes API (8080) and UI (5000) |

The server image (`orkesio/orkes-conductor-community`) ships both the Conductor API and the UI in a single container — the API is served on port 8080 and the UI via nginx on port 5000.

## Configuration

Server configuration lives in the `conductor-config` ConfigMap as a standard `.properties` file. The key settings:

```properties
conductor.db.type=postgres          # activates Postgres for both persistence and queuing
conductor.indexing.enabled=false    # no Elasticsearch required
spring.datasource.url=jdbc:postgresql://conductor-postgres:5432/conductor
spring.datasource.username=conductor
spring.datasource.password=conductor
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
loadSample=true                     # loads a sample workflow on first boot
```

To change any property, edit the ConfigMap and restart the Deployment:

```shell
kubectl edit configmap conductor-config -n conductor
kubectl rollout restart deployment/conductor-server -n conductor
```

## Using an external PostgreSQL instance

If you already have a managed PostgreSQL service (RDS, Cloud SQL, Azure Database, etc.), remove the `StatefulSet` and its `Service` from the manifest and update the ConfigMap's `spring.datasource.*` values to point at your external instance.

The database must exist before Conductor starts. Create it with:

```sql
CREATE DATABASE conductor;
```

Conductor applies its own schema on first boot.

## Production considerations

**Credentials** — Replace the plaintext `stringData` in the Secret with values from your secrets management system (Vault, AWS Secrets Manager, Sealed Secrets, etc.) before going to production.

**Replicas** — The Deployment defaults to 1 replica. Conductor server is stateless; scaling to 3+ replicas for HA requires no additional configuration beyond `replicas: 3`.

**Resources** — The manifest sets no resource requests or limits. For production, add appropriate values based on your workload. A starting point:

```yaml
resources:
  requests:
    cpu: "1000m"
    memory: "1500Mi"
  limits:
    cpu: "2000m"
    memory: "2500Mi"
```

**Ingress** — The manifest deliberately omits an Ingress so it works out of the box with any cluster. Add an Ingress resource targeting `conductor-server` on port 5000 (UI) and 8080 (API) using whatever IngressClass your cluster provides.

**Image version** — Pin the image tag to a specific release rather than using `latest` in production. Check [Docker Hub](https://hub.docker.com/r/orkesio/orkes-conductor-community/tags) for available tags.

## OpenShift

The manifest works on OpenShift with two adjustments:

**1. Replace Ingress with a Route**

Instead of a Kubernetes Ingress, create OpenShift Routes for the UI and API:

```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: conductor-ui
  namespace: conductor
spec:
  to:
    kind: Service
    name: conductor-server
  port:
    targetPort: ui
  tls:
    termination: edge
---
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: conductor-api
  namespace: conductor
spec:
  to:
    kind: Service
    name: conductor-server
  port:
    targetPort: api
  tls:
    termination: edge
```

**2. SecurityContextConstraints (SCC)**

The Conductor image runs as a non-root user. If your cluster enforces the `restricted` SCC, add a `securityContext` to the pod spec:

```yaml
securityContext:
  runAsNonRoot: true
  seccompProfile:
    type: RuntimeDefault
```

If Postgres fails to start due to volume permission issues under the `restricted` SCC, add:

```yaml
securityContext:
  fsGroup: 999
```

to the PostgreSQL pod spec (999 is the `postgres` user's GID in the official image).

**`oc` users** — all `kubectl` commands in this guide work identically with `oc`.
