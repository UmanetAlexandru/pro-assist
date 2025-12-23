# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache deps first
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY . .
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar (adjust if your jar name differs)
COPY --from=build /build/target/*.jar /app/app.jar

EXPOSE 8020
ENTRYPOINT ["java","-jar","/app/app.jar"]
