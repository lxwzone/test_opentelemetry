FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mv target/token-service-*.jar app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
