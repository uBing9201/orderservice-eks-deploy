# Base 이미지
FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar /app.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]