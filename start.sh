cd backend;
docker compose up -d;
SPRING_PROFILES_ACTIVE=postgres JPA_DDL_AUTO=update ./mvnw spring-boot:run;
