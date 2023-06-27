# Etapa de construção
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copia os arquivos do projeto
COPY pom.xml .
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests

# Etapa de execução
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copia o artefato construído da etapa anterior
COPY --from=build /app/target/payment-0.0.1-SNAPSHOT.jar .

# Executa o aplicativo
CMD ["java", "-jar", "payment-0.0.1-SNAPSHOT.jar"]
