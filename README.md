# PostgreSQL

Запуск в Docker:

//online-store-db

docker run --name product-db -p 5433:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=online-store postgres:16

// keycloak 
docker run --name online-store-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm
