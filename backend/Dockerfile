# Étape 1 : Build du projet avec Maven et OpenJDK 17
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copie des fichiers de configuration Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copie du code source et compilation
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Étape 2 : Image légère d'exécution
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
