# Deployment Guide: Smart Campus SaaS Platform

This document outlines the detailed steps required to deploy the Smart Campus Multi-Tenant SaaS platform to production. 

The architecture consists of:
* **Eureka Discovery Server** (Service Registry)
* **API Gateway** (Routing & JWT Filter)
* **6 Backend Microservices** (Tenant, Auth, Attendance, Library, Hostel, Fee)
* **PostgreSQL Database** (Multi-tenant schema-based isolation)

---

## 🚀 Option 1: VPS Deployment with Docker Compose (Recommended)

This is the most cost-effective and straightforward method for hosting an 8-service Spring Cloud stack. You can run the entire system on a single Virtual Private Server (VPS).

### System Requirements
* **Provider**: DigitalOcean, AWS, Hetzner, Linode, or similar.
* **Specs**: Minimum **4GB RAM** (8GB recommended for comfortable production headroom).
* **OS**: Ubuntu 22.04 LTS or newer.

---

### Step 1: Install Docker and Docker Compose on the VPS
Connect to your VPS via SSH and run:
```bash
# Update package index
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install docker.io -y

# Start and enable Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Install Docker Compose (v2)
sudo apt install docker-compose-plugin -y

# Verify installation
docker --version
docker compose version
```

---

### Step 2: Clone the Project
Clone the repository directly onto your VPS:
```bash
git clone https://github.com/Prabhas2277/smart-campus.git
cd smart-campus
```

---

### Step 3: Start the Production Stack
Run the following command from the root of the project to build the Docker images and start all containers in the background:
```bash
sudo docker compose up --build -d
```
*Docker Compose will automatically set up the Postgres database, start Eureka, compile the microservices, and boot the API Gateway.*

---

### Step 4: Verify Container Health
Check if all containers are running successfully:
```bash
sudo docker compose ps
```
You can also view the logs of any specific service (e.g., `api-gateway`):
```bash
sudo docker compose logs -f api-gateway
```

---

### Step 5: Configure Reverse Proxy (Nginx) & SSL
To expose your platform securely to the internet under your domain name:
1. **Install Nginx**:
   ```bash
   sudo apt install nginx -y
   ```
2. **Create Server Block**:
   Create `/etc/nginx/sites-available/smart-campus` and add the config:
   ```nginx
   server {
       listen 80;
       server_name yourdomain.com *.yourdomain.com; # Wildcard matches subdomains (mit.yourdomain.com)

       location / {
           proxy_pass http://localhost:8080; # Directs traffic to API Gateway
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```
3. **Enable Site & Restart Nginx**:
   ```bash
   sudo ln -s /etc/nginx/sites-available/smart-campus /etc/nginx/sites-enabled/
   sudo systemctl restart nginx
   ```
4. **Obtain SSL Certificate (Certbot)**:
   ```bash
   sudo apt install certbot python3-certbot-nginx -y
   sudo certbot --nginx -d yourdomain.com -d *.yourdomain.com
   ```

---

## ☁️ Option 2: PaaS Deployment (Render / Railway)

If you do not want to manage a VPS server, you can use cloud application platforms.

### Option 2A: Railway (Easiest Cloud Setup)
Railway natively supports deploying multi-container systems using `docker-compose.yml`.
1. Go to [Railway.app](https://railway.app/) and sign in with GitHub.
2. Click **New Project** -> **Deploy from GitHub repo**.
3. Choose the `smart-campus` repository.
4. Railway will detect the `docker-compose.yml` and spin up all containers (Postgres, Eureka, Gateway, Microservices) automatically.
5. In the settings of `api-gateway`, generate a public domain link.

---

### Option 2B: Render (Manual Service Provisioning)
Since Render does not support Docker Compose multi-container orchestrations out of the box, you must configure each service individually:

#### 1. Provision PostgreSQL
* Create a **Render PostgreSQL** database.
* Copy the **Internal Database URL** (e.g., `postgresql://postgres:password@host/db`).

#### 2. Deploy Eureka Discovery Server
* Create a **Web Service** on Render.
* Connect the `smart-campus` repository.
* Set the Root Directory to `eureka-server`.
* Select Runtime: `Docker`.
* Expose Port `8761`.

#### 3. Deploy Backend Microservices (Tenant, Auth, Attendance, Library, Hostel, Fee)
For *each* microservice:
* Create a **Private Service** (since these do not need public internet access; they route through the gateway).
* Set Root Directory to the respective folder (e.g. `tenant-service`).
* Select Runtime: `Docker`.
* Expose the correct service port (e.g. `8081`, `8082`).
* Add the following **Environment Variables**:
  * `SPRING_PROFILES_ACTIVE`: `postgres`
  * `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<your-render-db-internal-host>:5432/<db-name>`
  * `SPRING_DATASOURCE_USERNAME`: `<db-user>`
  * `SPRING_DATASOURCE_PASSWORD`: `<db-password>`
  * `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: `http://<eureka-service-private-url>:8761/eureka/`

#### 4. Deploy API Gateway
* Create a **Web Service** (needs to be public to serve frontend web traffic and receive requests).
* Set Root Directory to `api-gateway`.
* Select Runtime: `Docker`.
* Expose Port `8080`.
* Add Environment Variable:
  * `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: `http://<eureka-service-private-url>:8761/eureka/`

---

## 🗄️ Database Seeding in Production
Once your production stack is running, you can seed it by running the PowerShell script locally:
1. Open [seed.ps1](seed.ps1).
2. Modify the target gateway endpoint variable:
   ```powershell
   $GatewayUrl = "https://yourdomain.com" # Or your public Render/Railway domain
   ```
3. Run the script:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\seed.ps1
   ```
This will initialize your production environment with the standard MIT and Harvard tenant databases!
