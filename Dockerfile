FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

RUN mkdir -p /app/storage

ENTRYPOINT ["java", "-jar", "app.jar"]
