# LanguageSchoolHITS/Dockerfile
# ---- Общий этап для зависимостей ----
FROM maven:3.9.6-eclipse-temurin-21 AS deps

WORKDIR /app
COPY pom.xml .
COPY language-school-back/pom.xml language-school-back/
COPY language-school-app/pom.xml language-school-app/
RUN mvn dependency:go-offline -B

# ---- Бэкенд образ ----
FROM deps AS backend-build
COPY language-school-back/src language-school-back/src
RUN mvn clean package -pl language-school-back -am -DskipTests

FROM eclipse-temurin:21-jre-alpine AS backend
WORKDIR /app
COPY --from=backend-build /app/language-school-back/target/*.jar app.jar
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app
USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# ---- Фронтенд образ ----
FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY language-school-app/ ./
RUN npm ci --legacy-peer-deps && \
    npm run build -- --output-path=dist --configuration production

FROM nginx:latest AS frontend
COPY --from=frontend-build /app/dist/browser /app
COPY ./language-school-app/nginx.conf /app/nginx.conf
EXPOSE 80

RUN rm /etc/nginx/nginx.conf && cp /app/nginx.conf /etc/nginx/nginx.conf && rm /app/nginx.conf && cat /etc/nginx/nginx.conf
ENTRYPOINT ["nginx", "-g", "daemon off;"]