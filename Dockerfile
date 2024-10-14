# Используйте базовый образ с JDK
FROM openjdk:21-jdk-slim

WORKDIR /app

# Установка Docker CLI в контейнер
RUN apt-get update && apt-get install -y docker.io

# Копируем jar файл в контейнер
COPY build.gradle.kts /app/
COPY gradlew gradlew
COPY gradle /app/gradle


# Копируйте исходный код вашего приложения
COPY src /app/src

# Запустите Gradle для сборки приложения
RUN ./gradlew build -x test

# Копируйте файл .jar вашего приложения в контейнер
COPY build/libs/notion-0.0.1-SNAPSHOT.jar app.jar


# Укажите переменные окружения
ENV SPRING_APPLICATION_NAME=notion
ENV PORT=8080
ENV DATABASE_URL=jdbc:postgresql://postgres:5432/postgres
ENV DATABASE_USERNAME=postgres
ENV DATABASE_PASSWORD=postgres
ENV MINIO_ENDPOINT=http://minio1:9000
ENV MINIO_ACCESS_KEY=minioadmin
ENV MINIO_SECRET_KEY=minioadmin
ENV MINIO_BUCKET=my-bucket

# Откройте порт, который будет использоваться приложением
EXPOSE $PORT

# Запустите приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
