# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# 1. Copy only pom.xml first to cache dependencies
COPY pom.xml .

# 2. Download libraries (This matches your 2nd image for speed)
RUN mvn dependency:go-offline

# 3. Copy source code and build
COPY src ./src
RUN mvn clean package "-Dmaven.test.skip=true"

# Runtime Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 4. HIGHLIGHTED CHANGE: Using the Wildcard (*)
# This automatically finds "MovieBooking-0.0.1-SNAPSHOT.jar"
# and renames it to "app.jar" so the ENTRYPOINT never has to change.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# 5. HIGHLIGHTED CHANGE: Simplified Entrypoint
# Since we renamed the file to app.jar, this line is now "Permanent"
ENTRYPOINT ["java", "-jar", "app.jar"]