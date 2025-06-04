# PostgreSQL

Запуск в Docker:

//online-store-db

docker run --name online-store-db -p 5433:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=online-store postgres:16

// keycloak 
docker run --name online-store-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm

docker run --name online-store-metrics -p 8428:8428 -v .\config\victoria-metrics\promscrape.yaml:/promscrape.yaml victoriametrics/victoria-metrics:v1.93.12 --promscrape.config=/promscrape.yaml

docker run --name online-store-grafana -p 3000:3000 -v .\data\grafana:/var/lib/grafana grafana/grafana:10.2.4
