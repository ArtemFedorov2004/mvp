# PostgreSQL

Запуск в Docker:

//online-store-db

docker run --name product-db -p 5433:5432 -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=online-store postgres:16
