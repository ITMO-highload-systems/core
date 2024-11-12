# Используйте базовый образ с JDK
FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируйте файл .jar вашего приложения в контейнер
COPY build/libs/notion-0.0.1-SNAPSHOT.jar app.jar

RUN chmod +x /app/app.jar

RUN apt-get update && apt-get install -y docker.io

# Запустите приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
