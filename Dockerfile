# Usa um runtime Java 21 para executar o JAR.
FROM eclipse-temurin:21-jre
# Define o diretório de trabalho dentro da imagem.
WORKDIR /app
# Inclui somente o JAR executável produzido pelo Maven.
COPY target/produtos-0.0.1-SNAPSHOT.jar app.jar
# Documenta a porta que a API usa dentro do container.
EXPOSE 8080
# Inicia a aplicação sem depender de IDE ou Maven.
ENTRYPOINT ["java", "-jar", "app.jar"]
