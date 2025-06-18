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

mvn -f ./manager-app clean package
docker build --build-arg JAR_FILE=manager-app/target/manager-app-0.0.1-SNAPSHOT-exec.jar -t online-store/manager-app:0.0.1 .
docker run -p 8084:8084 -e SPRING_PROFILES_ACTIVE=docker --name online-store-manager-app online-store/manager-app:0.0.1