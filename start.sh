cd backend;
docker compose up -d --build;
cd ../frontend;
npm install;
npm start;
# SPRING_PROFILES_ACTIVE=postgres JPA_DDL_AUTO=update ./mvnw spring-boot:run;
