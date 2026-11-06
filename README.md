# RoboAdvisor Lite – End-to-End CI/CD Pipeline on AWS EC2 + Kubernetes (CIA 2)

A small Java (Spring Boot) robo-advisor web app that suggests an Equity / Debt / Gold
allocation from the investor's age and risk appetite and projects the value of a monthly SIP.
The project demonstrates a complete CI/CD pipeline:

**Git → GitHub Actions → Maven (build + JUnit tests) → Docker → Docker Hub → Ansible → Kubernetes (k3s) on an AWS EC2 instance**

## Project structure

```
robo-advisor-cicd/
├── pom.xml                          Maven build file
├── Dockerfile / .dockerignore       Container image definition
├── src/main/java/...                Spring Boot application (controller, service, model)
├── src/main/resources/static/       Web UI (index.html)
├── src/test/java/...                JUnit 5 unit tests (service + controller)
├── k8s/deployment.yaml              Kubernetes Deployment (2 replicas, probes, rolling update)
├── k8s/service.yaml                 Kubernetes LoadBalancer Service (port 80 -> 8080)
├── ansible/inventory.ini            Ansible inventory (CI runner = localhost)
├── ansible/deploy.yml               Ansible playbook that deploys/updates the app on k3s
└── .github/workflows/cicd.yml       GitHub Actions pipeline
```

## API

| Endpoint | Description |
|---|---|
| `GET /` | Web page |
| `GET /api/health` | `{"status":"UP","version":"1.0.0"}` – used by probes and pipeline |
| `GET /api/advice?age=21&risk=HIGH&amount=5000&years=10` | Allocation + SIP projection |

## One-time setup

### 1. AWS EC2 + k3s
EC2 console → **Launch instance**:
- Name `roboadvisor-k3s`, AMI **Ubuntu Server 24.04 LTS**, type **t3.small**
- Key pair: create `roboadvisor-key` (.pem)
- Security group inbound rules: SSH 22, HTTP 80, Custom TCP 6443 (source Anywhere)
- Advanced details → **User data**:

```bash
#!/bin/bash
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
PUBIP=$(curl -s https://checkip.amazonaws.com)
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--tls-san $PUBIP --disable traefik --write-kubeconfig-mode 644" sh -
```

After 2–3 minutes connect with **EC2 Instance Connect** and run:

```bash
kubectl get nodes
sed "s/127.0.0.1/$(curl -s https://checkip.amazonaws.com)/" /etc/rancher/k3s/k3s.yaml
```

Copy the whole output into the GitHub secret `KUBE_CONFIG`.

### 2. Docker Hub
Create a free account and an access token (Account Settings → Personal access tokens, Read & Write).

### 3. GitHub repository secrets
Settings → Secrets and variables → Actions → New repository secret:

| Secret | Value |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `KUBE_CONFIG` | kubeconfig of the k3s cluster (with the EC2 public IP) |

Then Actions → CI-CD Pipeline → **Run workflow** (or push any commit).

## Change and redeploy demo
1. Edit `src/main/resources/application.properties` → `app.version=2.0.0`
2. `git commit -am "Update version to 2.0.0" && git push`
3. The pipeline runs automatically, builds a new image tagged with the commit SHA,
   and Ansible rolls it out on k3s. The new version appears in the page footer.

## Run locally (optional)
```bash
mvn clean package
docker build -t robo-advisor:local .
docker run -p 8080:8080 robo-advisor:local
```

## Clean up (to save AWS credit)
EC2 console → select `roboadvisor-k3s` → **Instance state → Terminate instance**.
Then delete the security group and key pair (optional, they are free).
Do not just *stop* the instance: the public IP changes on restart and the kubeconfig stops working.
