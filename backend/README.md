# Backend - Spring Boot (E-Commerce Java)

README này tổng hợp các lệnh cần thiết để chạy backend và mô tả cấu trúc thư mục Spring Boot trong dự án.

## 1) Yêu cầu môi trường

- Java 17
- Docker + Docker Compose (nếu chạy theo container)
- MySQL 8 và Redis (nếu chạy local không qua Docker Compose)

## 2) Biến môi trường cần có

File `src/main/resources/application.properties` đang đọc giá trị từ biến môi trường. Bạn cần khai báo tối thiểu:

- `SPRING_APPLICATION_NAME`
- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_OPEN_IN_VIEW`
- `SPRING_JPA_FORMAT_SQL`
- `SPRING_JPA_DATABASE_PLATFORM`
- `SPRING_FLYWAY_ENABLED`
- `SPRING_FLYWAY_LOCATIONS`
- `SPRING_FLYWAY_BASELINE_ON_MIGRATE`
- `SPRING_FLYWAY_BASELINE_VERSION`
- `SPRING_WEB_RESOURCES_STATIC_LOCATIONS`
- `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE`
- `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION_MS`
- `JWT_REFRESH_EXPIRATION_MS`

Nếu chạy bằng `docker-compose.yml` ở thư mục gốc, bạn cũng cần các biến DB:

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

## 3) Các lệnh cần thiết (local)

Chạy trong thư mục `backend/`.

```bash
cd backend
chmod +x gradlew
```

### Cài dependency và build

```bash
./gradlew clean build
```

### Chạy ứng dụng Spring Boot

```bash
./gradlew bootRun
```

### Chạy test

```bash
./gradlew test
```

### Tạo file jar

```bash
./gradlew bootJar
```

## 4) Các lệnh cần thiết (Docker Compose)

Chạy trong thư mục gốc dự án `E-Commerce_Java/`.

```bash
docker compose up --build
```

Lệnh hữu ích:

```bash
docker compose ps
docker compose logs -f backend
docker compose down
docker compose down -v
```

## 5) Cấu trúc file Spring Boot

```text
backend/
|-- build.gradle.kts
|-- settings.gradle.kts
|-- Dockerfile
|-- gradlew
|-- src/
|   |-- main/
|   |   |-- java/com/hoang/backend/
|   |   |   |-- BackendApplication.java          # điểm vào chính
|   |   |   |-- common/                          # xử lý dùng chung (exception, helper...)
|   |   |   |-- config/                          # cấu hình Spring (bảo mật, bean...)
|   |   |   |-- modules/
|   |   |   |   |-- users/
|   |   |   |   |   |-- controller/
|   |   |   |   |   |-- dto/
|   |   |   |   |   |-- entity/
|   |   |   |   |   |-- repository/
|   |   |   |   |   |-- security/
|   |   |   |   |   `-- service/
|   |   |   |   |-- products/
|   |   |   |   |-- orders/
|   |   |   |   |-- cart/
|   |   |   |   |-- payments/
|   |   |   |   |-- wishlist/
|   |   |   |   |-- reviews/
|   |   |   |   `-- adminpanel/
|   |   `-- resources/
|   |       |-- application.properties           # map biến môi trường
|   |       |-- db/migration/                    # script migration Flyway
|   |       |-- static/
|   |       `-- templates/
|   `-- test/java/com/hoang/backend/
|       `-- BackendApplicationTests.java
|-- media/
`-- staticfiles/
```

## 6) Quy ước nhanh cho từng module

Mỗi module thường theo flow:

`controller -> service -> repository -> entity`

- `controller`: nhận HTTP request/response
- `service`: xử lý business logic
- `repository`: truy cập CSDL qua JPA
- `entity`: định nghĩa bảng/quan hệ dữ liệu
- `dto`: request/response object

