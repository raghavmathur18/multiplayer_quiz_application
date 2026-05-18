# Stage 1: Build the React Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot Backend
FROM gradle:8.4-jdk17-alpine AS backend-build
WORKDIR /app/backend
# Copy the gradle wrapper and properties
COPY backend/gradlew backend/build.gradle backend/settings.gradle ./
COPY backend/gradle ./gradle
# Copy the source code
COPY backend/src ./src
# Copy the built frontend into static resources
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
# Build the application
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Stage 3: Run the Application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the jar from backend-build
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080

# Set JVM memory limits to prevent OOM kills on Render's free tier (512MB max)
ENV JAVA_OPTS="-Xmx256m -Xms128m -Xss512k -XX:MaxMetaspaceSize=128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
