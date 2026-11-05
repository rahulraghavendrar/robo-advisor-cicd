# RoboAdvisor Lite – End-to-End CI/CD Pipeline on Azure (CIA 2)

A small Java (Spring Boot) robo-advisor web app that suggests an Equity / Debt / Gold
allocation from the investor's age and risk appetite and projects the value of a monthly SIP.
The project demonstrates a complete CI/CD pipeline:

**Git → GitHub Actions → Maven (build + JUnit tests) → Docker → Docker Hub → Ansible → Azure Kubernetes Service (AKS)**

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
├── ansible/deploy.yml               Ansible playbook that deploys/updates the app on AKS
└── .github/workflows/cicd.yml       GitHub Actions pipeline
```

## API

| Endpoint | Description |
|---|---|
| `GET /` | Web page |
| `GET /api/health` | `{"status":"UP","version":"1.0.0"}` – used by probes and pipeline |
| `GET /api/advice?age=21&risk=HIGH&amount=5000&years=10` | Allocation + SIP projection |

## One-time setup

### 1. Azure (Azure for Students – free credit, no credit card)
Open **Azure Cloud Shell (Bash)** in the Azure portal and run:

```bash
az provider register --namespace Microsoft.ContainerService
az group create --name rg-roboadvisor --location centralindia
az aks create --resource-group rg-roboadvisor --name aks-roboadvisor \
  --tier free --node-count 1 --node-vm-size Standard_B2s --generate-ssh-keys
az aks get-credentials --resource-group rg-roboadvisor --name aks-roboadvisor --file kubeconfig.yaml
cat kubeconfig.yaml
```

If `Standard_B2s` or the region is not allowed, try `--node-vm-size Standard_B2s_v2`
or `Standard_D2s_v3`, or location `southindia` / `eastus`.

### 2. Docker Hub
Create a free account and an access token (Account Settings → Personal access tokens, Read & Write).

### 3. GitHub repository secrets
Settings → Secrets and variables → Actions → New repository secret:

| Secret | Value |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `KUBE_CONFIG` | full contents of `kubeconfig.yaml` |

Then Actions → CI-CD Pipeline → **Run workflow** (or push any commit).

## Change and redeploy demo
1. Edit `src/main/resources/application.properties` → `app.version=2.0.0`
2. `git commit -am "Update version to 2.0.0" && git push`
3. The pipeline runs automatically, builds a new image tagged with the commit SHA,
   and Ansible rolls it out on AKS. The new version appears in the page footer.

## Run locally (optional)
```bash
mvn clean package
docker build -t robo-advisor:local .
docker run -p 8080:8080 robo-advisor:local
```

## Clean up (to save Azure credit)
```bash
az group delete --name rg-roboadvisor --yes --no-wait
```
