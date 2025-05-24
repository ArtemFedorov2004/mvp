# PostgreSQL

Запуск в Docker:

docker run --name product-db -p 5433:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=product postgres:16