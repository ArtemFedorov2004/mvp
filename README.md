# Online Store Platform

**Основные компоненты системы**

1. Ядро системы ([online-store-service](#online-store-service))

    * REST API сервис, обрабатывающий всю бизнес-логику

2. Клиентские приложения

    * [customer-app](#customer-app): Веб приложение для покупателей
    * [manager-app](#manager-app): Веб приложение для менеджеров

3. Внешние сервисы

    * [admin-server](#admin-server): Мониторинг приложений
    * [victoria-metrics](#victoriametrics): Система сбора и хранения метрик
    * [grafana](#grafana): Визуализация метрик и дашборды
    * `Keycloak`: Система аутентификации и авторизации
    * `PostgreSQL`: База данных

## Online Store Service

### Функциональные возможности

Для менеджеров:

* Создание новых товаров с указанием названия и цены
* Редактирование существующих товаров (изменение названия, цены)
* Удаление товаров
* Просмотр полного списка товаров

Для покупателей:

* Просмотр товаров
* Оставление отзывов о товаре

### Модель данных

**Товары (`Products`)**

Каждый товар в системе содержит:

* Уникальный идентификатор
* Название (обязательное поле, от `1` до `50` символов)
* Цену (обязательное положительное число)

**Отзывы (`Reviews`)**

Отзывы включают:

* Оценку (целое число от `1` до `5`)
* Дату создания (`timestamp`)
* Текстовые поля: достоинства, недостатки, комментарий

Каждый отзыв связан с товаром и автором отзыва

### Миграции базы данных (Flyway)

Система использует Flyway для управления миграциями базы данных. Все объекты базы данных создаются в схеме
`online_store`.

Скрипты расположены в `db/migration`:

**`V0.0.1__Basic_schema.sql` - Базовая схема:**

1. Создание схемы `online_store`
2. Таблицы товаров (`t_product`) и пользователей (`t_customer`)
3. Таблица связи с Keycloak (`t_customer_oidcuser`)
    * Связывает внутренний UUID пользователя с идентификатором в Keycloak

**`V0.0.2__Review_table.sql` - Система отзывов:**

* Таблица отзывов (`t_review`)
* Связи с товарами и пользователями

### ORM

Система использует **Java Persistence API (JPA)** с реализацией **Hibernate** для работы с базой данных.

### Локализация и обработка ошибок

Система поддерживает локализацию сообщений об ошибках (в настоящее время только на русском языке). Все входящие данные
проходят валидацию.
Файл `messages.properties` содержит все сообщения об ошибках.
Система возвращает ошибки в формате Problem Details:

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Товар не найден",
  "instance": "/online-store-api/products/10"
}
```

### Безопасность

Система использует OAuth2/OpenID Connect для аутентификации через Keycloak. Доступ к функциям контролируется через
scopes:

* **Просмотр товаров** - доступен всем
* **Управление товарами** - требует scope `edit_products`
* **Создание отзывов** - требует scope `create_product_review`
* **Мониторинг** - требует scope `metrics`

### Интеграция с Admin Server

**Необходимые зависимости**

```xml

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Настройка клиента в Keycloak**

В Keycloak заведен отдельный клиент для мониторинга:

* **Client ID**: `online-store-service-metrics-client`
* **Scopes** `metrics_server`

**Защита Actuator эндпоинтов**

```java
.requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
```

**Регистрация в Admin Server:**

* Сервис использует `online-store-service-metrics-client` для получения токена
* Токен содержит scope `metrics_server` для аутентификации

Для регистрации в Spring Boot Admin используется специальный `RegistrationClient`, который:

* Создается только при включенном свойстве `spring.boot.admin.client.enabled=true`
* Добавляет JWT-токен в заголовки всех запросов к Admin Server

### Конфигурации

**Профили Spring Boot:**

1. `standalone` - Локальная разработка
2. `docker` - Docker Compose
3. `k8s` - Kubernetes

### Запуск приложения

**Локальная разработка**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=standalone
```

**Docker**

Сборка и запуск контейнера для текущей версии приложения (0.0.1-SNAPSHOT):

```bash
docker build --build-arg JAR_FILE=online-store-service/target/online-store-service-0.0.1-SNAPSHOT-exec.jar -t online-store/online-store-service:0.0.1 .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker --name online-store-online-store-service online-store/online-store-service:0.0.1
```

### Развертывание в Kubernetes

Перед развертыванием необходимо подготовить Docker-образ приложения:

```bash
docker tag online-store/online-store-service:0.0.1 localhost:5000/online-store/online-store-service:0.0.1
docker push localhost:5000/online-store/online-store-service:0.0.1

minikube ssh docker pull 192.168.49.1:5000/online-store/online-store-service:0.0.1
```

**Конфигурация приложения**

Для работы в Kubernetes приложению требуется ConfigMap, который создается из файла application-k8s.yaml:

```bash
kubectl create configmap online-store-service-config \
  --from-file=application-k8s.yaml=online-store-service/src/main/resources/application-k8s.yaml
```

Каждый под содержит два контейнера:

* Основное приложение
* vmagent

**vmagent**

* Собирает метрики с основного приложения
* Отправляет их в сервис VictoriaMetrics

**Применение**

```bash
kubectl apply -f ./k8s/services/online-store-service-deployment.yaml
```

### Helm-развертывание

Helm загружает общие шаблоны из shared-чарта

**Структура чарта:**

1. **Chart.yaml** - метаинформация + указание зависимости от shared-чарта
2. **values.yaml** - параметры развертывания
3. **Папка config** - содержит:
    * application-k8s.yaml - Spring Boot конфигурация
    * promscrape.yaml - настройки сбора метрик

**Обновление зависимостей чарта**

Перед установкой или обновлением чарта необходимо выполнить команду:
```bash
helm dependency update .
```

**Основная команда развертывания:**
```bash
helm install service .
```

### Тестирование

Запуск тестов

```bash
# Все тесты
mvn clean verify

# Только unit-тесты
mvn clean test

# Только интеграционные тесты
mvn failsafe:integration-test
```

Для проверки работы с реальной базой данных используется Testcontainers.

### API Документация

Документация доступна через Swagger UI:

* `/swagger-ui.html` - Интерфейс Swagger
* `/v3/api-docs` - OpenAPI спецификация

## Customer App

Веб-приложение для покупателей

### Основной функционал

1. Просмотр списка товаров
2. Просмотр конкретного товара
3. Создание отзывов о товарах
4. Просмотр отзывов о товаре

### Thymeleaf Шаблоны

Шаблоны приложения расположены в стандартной директории `src/main/resources/templates` и организованы по следующим
категориям:

* **Основные страницы:**

    * `online-store/products/list.html` - страница списка товаров
    * `online-store/products/product.html` - страница товара
    * `online-store/products/reviews/new_review.html` - форма создания отзыва

* **Страницы ошибок:**
    * `errors/404.html` - страница "Не найдено"

При наличии ошибок (`errors` в модели) отображается блок с сообщениями:

```html

<div data-th-if="${errors}">
    <ul>
        <li
                data-th-each="error : ${errors}"
                data-th-text="${error}"
                style="color: red"
        ></li>
    </ul>
</div>
```

### Локализация

Система поддерживает локализацию сообщений об ошибках (в настоящее время только на русском языке).
Файл `messages.properties` содержит все сообщения об ошибках.

### Интеграция с REST API

**Клиенты для работы с API**

* **ProductsRestClient**: Работа с товарами
* **ReviewsRestClient**: Работа с отзывами

### Безопасность

**Аутентификация**

* Реализована через Keycloak по протоколу OAuth2/OpenID Connect
* После успешного входа - перенаправление на страницу списка товаров

**Авторизация**

* Просмотр товаров - доступен всем
* Создание отзывов - требует scope `create_product_review`
* Все остальные запросы - запрещены

Все запросы к API содержат Bearer Token

### Интеграция с Admin Server

**Необходимые зависимости**

```xml

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Механизм регистрации в Admin Server**

Приложение автоматически регистрируется в Spring Boot Admin при старте:

* Использует отдельного Keycloak-клиента (`customer-app-metrics-client`)
* Токен содержит scope `metrics_server` для аутентификации
* Все запросы к Admin Server автоматически идут с JWT-токеном

**Защита Actuator эндпоинтов**

Эндпоинты мониторинга `/actuator/**` требуют наличия JWT-токена с scope `metrics`

### Запуск приложения

**Профили конфигурации**

Доступно 3 профиля Spring Boot:

1. **standalone** - Локальная разработка
2. **docker** - Запуск в Docker-окружении
3. **k8s** - Развертывание в Kubernetes

**Локальная разработка**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=standalone
```

**Docker**

Сборка и запуск контейнера для текущей версии приложения (0.0.1-SNAPSHOT):

```bash
docker build --build-arg JAR_FILE=customer-app/target/customer-app-0.0.1-SNAPSHOT-exec.jar -t online-store/customer-app:0.0.1 .
docker run -p 8081:8081 -e SPRING_PROFILES_ACTIVE=docker --name online-store-customer-app online-store/customer-app:0.0.1
```

### Развертывание в Kubernetes

Перед развертыванием необходимо подготовить Docker-образ приложения:

```bash
docker tag online-store/customer-app:0.0.1 localhost:5000/online-store/customer-app:0.0.1
docker push localhost:5000/online-store/customer-app:0.0.1

minikube ssh docker pull 192.168.49.1:5000/online-store/customer-app:0.0.1
```

**Конфигурация приложения**

Для работы в Kubernetes приложению требуется ConfigMap, который создается из файла application-k8s.yaml:

```bash
kubectl create configmap online-store-customer-app-config \
  --from-file=application-k8s.yaml=customer-app/src/main/resources/application-k8s.yaml
```

Каждый под содержит два контейнера:

* Основное приложение
* vmagent

**vmagent**

* Собирает метрики с основного приложения
* Отправляет их в сервис VictoriaMetrics

**Применение**

```bash
kubectl apply -f ./k8s/services/customer-app-deployment.yaml
```

### Helm-развертывание

Helm загружает общие шаблоны из shared-чарта

**Структура чарта:**

1. **Chart.yaml** - метаинформация + указание зависимости от shared-чарта
2. **values.yaml** - параметры развертывания
3. **Папка config** - содержит:
    * application-k8s.yaml - Spring Boot конфигурация
    * promscrape.yaml - настройки сбора метрик

**Обновление зависимостей чарта**

Перед установкой или обновлением чарта необходимо выполнить команду:
```bash
helm dependency update .
```

**Основная команда развертывания:**
```bash
helm install customer-app .
```

### Тестирование

Запуск тестов

```bash
# Все тесты
mvn clean verify

# Только unit-тесты
mvn clean test

# Только интеграционные тесты
mvn failsafe:integration-test
```

Для интеграционных тестов используется Wiremock, чтобы замокать обращение к сервису.

## Manager App

Это веб-приложение для менеджеров, предоставляющее полный функционал управления товарами.

### Основной функционал

1. **Просмотр списка** всех товаров
2. **Просмотр** отдельного товара
3. **Создание** новых товаров
4. **Редактирование** существующих товаров
5. **Удаление** товаров из системы

### Thymeleaf Шаблоны

Шаблоны приложения расположены в стандартной директории `src/main/resources/templates` и организованы по следующим
категориям:

* **Основные страницы:**

    * `online-store/products/list.html` - страница списка товаров
    * `online-store/products/product.html` - страница товара
    * `online-store/products/edit.html` - форма редактирования товара
    * `online-store/products/new_product.html` - форма создания нового товара

* **Страницы ошибок:**
    * `errors/404.html` - страница "Не найдено"

При наличии ошибок (`errors` в модели) отображается блок с сообщениями:

```html

<div data-th-if="${errors}">
    <ul>
        <li
                data-th-each="error : ${errors}"
                data-th-text="${error}"
                style="color: red"
        ></li>
    </ul>
</div>
```

### Локализация

Система поддерживает локализацию сообщений об ошибках (в настоящее время только на русском языке).
Файл `messages.properties` содержит все сообщения об ошибках.

### Интеграция с REST API

**Клиенты для работы с API**

* **ProductsRestClient**: Работа с товарами

### Безопасность

**Аутентификация**

* Реализована через Keycloak по протоколу OAuth2/OpenID Connect
* После успешного входа - перенаправление на страницу списка товаров

**Авторизация**

* **Доступ только для роли MANAGER**

Все запросы к API содержат Bearer Token

### Интеграция с Admin Server

**Необходимые зависимости**

```xml

<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Механизм регистрации в Admin Server**

Приложение автоматически регистрируется в Spring Boot Admin при старте:

* Использует отдельного Keycloak-клиента (`manager-app-metrics-client`)
* Токен содержит scope `metrics_server` для аутентификации
* Все запросы к Admin Server автоматически идут с JWT-токеном

**Защита Actuator эндпоинтов**

Эндпоинты мониторинга `/actuator/**` требуют наличия JWT-токена с scope `metrics`

### Запуск приложения

**Профили конфигурации**

Доступно 3 профиля Spring Boot:

1. **standalone** - Локальная разработка
2. **docker** - Запуск в Docker-окружении
3. **k8s** - Развертывание в Kubernetes

**Локальная разработка**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=standalone
```

**Docker**

Сборка и запуск контейнера для текущей версии приложения (0.0.1-SNAPSHOT):

```bash
docker build --build-arg JAR_FILE=manager-app/target/manager-app-0.0.1-SNAPSHOT-exec.jar -t online-store/manager-app:0.0.1 .
docker run -p 8084:8084 -e SPRING_PROFILES_ACTIVE=docker --name online-store-manager-app online-store/manager-app:0.0.1
```

### Развертывание в Kubernetes

Перед развертыванием необходимо подготовить Docker-образ приложения:

```bash
docker tag online-store/manager-app:0.0.1 localhost:5000/online-store/manager-app:0.0.1
docker push localhost:5000/online-store/manager-app:0.0.1

minikube ssh docker pull 192.168.49.1:5000/online-store/manager-app:0.0.1
```

**Конфигурация приложения**

Для работы в Kubernetes приложению требуется ConfigMap, который создается из файла application-k8s.yaml:

```bash
kubectl create configmap online-store-manager-app-config \
  --from-file=application-k8s.yaml=manager-app/src/main/resources/application-k8s.yaml
```

Каждый под содержит два контейнера:

* Основное приложение
* vmagent

**vmagent**

* Собирает метрики с основного приложения
* Отправляет их в сервис VictoriaMetrics

**Применение**

```bash
kubectl apply -f ./k8s/services/manager-app-deployment.yaml
```

### Helm-развертывание

Helm загружает общие шаблоны из shared-чарта

**Структура чарта:**

1. **Chart.yaml** - метаинформация + указание зависимости от shared-чарта
2. **values.yaml** - параметры развертывания
3. **Папка config** - содержит:
    * application-k8s.yaml - Spring Boot конфигурация
    * promscrape.yaml - настройки сбора метрик

**Обновление зависимостей чарта**

Перед установкой или обновлением чарта необходимо выполнить команду:
```bash
helm dependency update .
```

**Основная команда развертывания:**
```bash
helm install manager-app .
```

### Тестирование

Запуск тестов

```bash
# Все тесты
mvn clean verify

# Только unit-тесты
mvn clean test

# Только интеграционные тесты
mvn failsafe:integration-test
```

Для интеграционных тестов используется Wiremock, чтобы замокать обращение к сервису.


## Admin Server

Обеспечивает **мониторинг** всех сервисов

### Как работает

1. **Клиенты регистрируются** при старте (используют `client_credentials`).
2. **Admin Server** запрашивает `/actuator/**` у сервисов (со scope `metrics`).
3. **Пользователи** входят в веб-интерфейс через Keycloak.

### Конфигурация безопасности

1. **Для API** (регистрация + актуаторы):
    * Stateless, JWT, scope `metrics_server`/`metrics`.
2. **Для UI:**
    * Stateful, OAuth2 Login.

### Запуск приложения

**Профили конфигурации**

Доступно 3 профиля Spring Boot:

1. **standalone** - Локальная разработка
2. **docker** - Запуск в Docker-окружении
3. **k8s** - Развертывание в Kubernetes

**Локальная разработка**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=standalone
```

**Docker**

Сборка и запуск контейнера для текущей версии приложения (0.0.1-SNAPSHOT):

```bash
docker build --build-arg JAR_FILE=admin-server/target/admin-server-0.0.1-SNAPSHOT-exec.jar -t online-store/admin-server:0.0.1 .
docker run -p 8083:8083 -e SPRING_PROFILES_ACTIVE=docker --name online-store-admin-server online-store/admin-server:0.0.1
```

### Развертывание в Kubernetes

Перед развертыванием необходимо подготовить Docker-образ приложения:

```bash
docker tag online-store/admin-server:0.0.1 localhost:5000/online-store/admin-server:0.0.1
docker push localhost:5000/online-store/admin-server:0.0.1

minikube ssh docker pull 192.168.49.1:5000/online-store/admin-server:0.0.1
```

**Конфигурация приложения**

Для работы в Kubernetes приложению требуется ConfigMap, который создается из файла application-k8s.yaml:

```bash
kubectl create configmap online-store-admin-server-config \
  --from-file=application-k8s.yaml=admin-server/src/main/resources/application-k8s.yaml
```

Каждый под содержит два контейнера:

* Основное приложение
* vmagent

**vmagent**

* Собирает метрики с основного приложения
* Отправляет их в сервис VictoriaMetrics

**Применение**

```bash
kubectl apply -f ./k8s/services/admin-server-deployment.yaml
```

### Helm-развертывание

Helm загружает общие шаблоны из shared-чарта

**Структура чарта:**

1. **Chart.yaml** - метаинформация + указание зависимости от shared-чарта
2. **values.yaml** - параметры развертывания
3. **Папка config** - содержит:
   * application-k8s.yaml - Spring Boot конфигурация
   * promscrape.yaml - настройки сбора метрик

**Обновление зависимостей чарта**

Перед установкой или обновлением чарта необходимо выполнить команду:
```bash
helm dependency update .
```

**Основная команда развертывания:**
```bash
helm install admin-server .
```


## Keycloak

**Запуск Keycloak в Docker**

Для локальной разработки Keycloak запускается следующей командой:

```bash
docker run --name online-store-keycloak -p 8082:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v ./config/standalone/keycloak/import:/opt/keycloak/data/import \
  quay.io/keycloak/keycloak:23.0.4 \
  start-dev --import-realm
```

**Структура конфигурации**

В проекте существует несколько вариантов конфигурации Keycloak:

```text
config/
├── standalone/          # Конфигурация для локального запуска
│   └── keycloak/
│       └── import/
│           └── online-store.json
├── docker/             # Конфигурация для Docker Compose
│   └── keycloak/
│       └── import/
│           └── online-store.json
└── k8s/                # Конфигурация для Kubernetes
    └── keycloak/
        └── import/
            └── online-store.json
```

**Запуск Keycloak в Kubernetes**

**Создание ConfigMap:**
```bash
kubectl create configmap online-store-keycloak-realm \
  --from-file=./config/k8s/keycloak/import/online-store.json
```

**Применение конфигурации:**
```bash
kubectl apply -f ./k8s/infrastructure/keycloak.yaml
```


## PostgreSQL

**Запуск PostgreSQL в Docker**

Для локальной разработки база данных запускается следующей командой:

```bash
docker run --name online-store-db -p 5433:5432 \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=online-store \
  postgres:16
```

**Настройка PostgreSQL в Kubernetes**

Для развертывания PostgreSQL выполните команду:

```bash
kubectl apply -f ./k8s/infrastructure/db.yaml
```

## VictoriaMetrics

**Запуск VictoriaMetrics в Docker**

Для сбора метрик используется команда:

```bash
docker run --name online-store-metrics -p 8428:8428 \
  -v ./config/docker/victoria-metrics/promscrape.yaml:/promscrape.yaml \
  victoriametrics/victoria-metrics:v1.93.12 \
  --promscrape.config=/promscrape.yaml
```

**Конфигурация сбора метрик**

Файл **promscrape.yaml** содержит настройки для четырех сервисов:

1. Admin Server
2. Customer App
3. Online Store Service
4. Manager App

**Механизм аутентификации**

* **Клиент в Keycloak:** `victoria-metrics`
* **Scope:** `metrics`

**Настройка VictoriaMetrics в Kubernetes**

Для развертывания выполните:
```bash
kubectl apply -f ./k8s/infrastructure/victoria-metrics.yaml
```

## Grafana

**Запуск Grafana в Docker**

Для локальной разработки Grafana запускается командой:

```bash
docker run --name online-store-grafana -p 3000:3000 \
  -v ./data/grafana:/var/lib/grafana \
  -u "$(id -u)" \
  grafana/grafana:10.2.4
```

**Хранение данных**

Все настройки и дашборды сохраняются в `./data/grafana`

**Настройка Grafana в Kubernetes**

Для развертывания Grafana выполните команду:
```bash
kubectl apply -f ./k8s/infrastructure/grafana.yaml
```

## Docker-образ для Spring Boot приложений

Используется **многоэтапная сборка**:

1. Этап распаковки: извлечение слоев JAR-файла
2. Этап сборки: создание итогового образа

**Распаковка JAR:**

1. Принимает аргумент `JAR_FILE` (путь к jar-файлу)
2. Использует Spring Boot Layertools для распаковки:
    * Разделяет на слои согласно` layers.idx`

**Финальный образ:**

* Создается отдельная группа `spring-boot-group`
* Добавляется пользователь `spring-boot`
* Все дальнейшие команды выполняются от этого пользователя

Делаем это для того, чтобы все действия выполнялись в рамках данного контейнера от имени пользователя отличного от root

**Запуск приложения:**

* `${JAVA_OPTS}`: переменная для JVM-флагов
* `${0} ${@}`: передача аргументов командной строки

## Docker Compose

Файл `docker-compose.yaml` разворачивает полную среду для разработки и тестирования платформы

Все Spring Boot приложения запускаются с профилем `docker`.

**Запуск**

**Требуется** предварительно собрать JAR-файлы

**Команда запуска**

```bash
docker compose up -d
```


## Kubernetes

**Запуск локального Docker Registry**

Перед работой с образами необходимо запустить локальный registry:
```bash
docker start registry
```

**Подготовка Minikube**

Перед развертыванием выполняем:
```bash
minikube start --driver=docker --insecure-registry="192.168.49.1/24" --addons="ingress"
kubectl config set-context --current --namespace=online-store
```

**Структура папки k8s**

```text
k8s/
├── infrastructure/   # Инфраструктурные компоненты
├── services/         # Приложения
└── helm/             # Helm-чарты
```


## Helm

**Библиотечный чарт (shared)**

Выступает как фундамент для всех сервисных чартов.


# Лицензия

Apache License. Подробнее см. в файле [LICENSE](LICENSE).