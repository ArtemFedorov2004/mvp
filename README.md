# PostgreSQL

Запуск в Docker:

//online-store-db

docker run --name online-store-db -p 5433:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=online-store postgres:16

// keycloak 
docker run --name online-store-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/standalone/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm

docker run --name online-store-metrics -p 8428:8428 -v ./config/docker/victoria-metrics/promscrape.yaml:/promscrape.yaml victoriametrics/victoria-metrics:v1.93.12 --promscrape.config=/promscrape.yaml

docker run --name online-store-grafana -p 3000:3000 -v ./data/grafana:/var/lib/grafana -u "$(id -u)" grafana/grafana:10.2.4

mvn -f ./admin-server clean package
docker build --build-arg JAR_FILE=admin-server/target/admin-server-0.0.1-SNAPSHOT-exec.jar -t online-store/admin-server:0.0.1 .
docker run -p 8083:8083 -e SPRING_PROFILES_ACTIVE=docker --name online-store-admin-server online-store/admin-server:0.0.1

mvn -f ./customer-app clean package
docker build --build-arg JAR_FILE=customer-app/target/customer-app-0.0.1-SNAPSHOT-exec.jar -t online-store/customer-app:0.0.1 .
docker run -p 8081:8081 -e SPRING_PROFILES_ACTIVE=docker --name online-store-customer-app online-store/customer-app:0.0.1

mvn -f ./online-store-service clean package
docker build --build-arg JAR_FILE=online-store-service/target/online-store-service-0.0.1-SNAPSHOT-exec.jar -t online-store/online-store-service:0.0.1 .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker --name online-store-online-store-service online-store/online-store-service:0.0.1

MY SCRIPT
# install git
sudo apt update
sudo apt install git
git --version
git config --global user.name "ArtemFedorov2004"
git config --global user.email "artem20.fedorov00@mail.com"

# install docker
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo groupadd docker
sudo usermod -aG docker $USER
newgrp docker

# install vscode
 # wget -qO- https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > packages.microsoft.gpg
 # sudo install -o root -g root -m 644 packages.microsoft.gpg /usr/share/keyrings/
  # sudo sh -c 'echo "deb [arch=amd64 signed-by=/usr/share/keyrings/packages.microsoft.gpg] https://packages.microsoft.com/repos/vscode stable main" > /etc/apt/sources.list.d/vscode.list'
   # sudo apt update
 # sudo apt install code
 
# install my project
cd ~/Desktop
git clone https://github.com/ArtemFedorov2004/mvp.git
mv ~/Desktop/mvp ~/Desktop/online-store-parent

# open in firefox
https://github.com/alex-kosarev/sc24
https://github.com/ArtemFedorov2004/mvp

# install java
sudo apt update
sudo apt upgrade
sudo apt search openjdk
sudo apt install openjdk-21-jdk
nano ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
source ~/.bashrc
echo $JAVA_HOME

# install maven
sudo apt update    # Просим Ubuntu обновить список доступных пакетов
sudo apt install maven    # Устанавливаем Maven
mvn -version

# install vim
sudo apt install vim

# install k8s
curl -LO https://dl.k8s.io/release/`curl -LS https://dl.k8s.io/release/stable.txt`/bin/linux/amd64/kubectl
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin/kubectl
kubectl version --client

curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 \
  && chmod +x minikube
  
  sudo mkdir -p /usr/local/bin/
sudo install minikube /usr/local/bin/

minikube start --driver=docker --insecure-registry="192.168.49.1/24" --addons="ingress"
