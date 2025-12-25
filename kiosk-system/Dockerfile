FROM amazoncorretto:17

WORKDIR /app

# WAŻNE: Obraz amazoncorretto jest minimalny i brakuje w nim "tar" oraz "gzip".
# Instalujemy je ręcznie, aby Maven Wrapper mógł się poprawnie rozpakować.
RUN yum install -y tar gzip

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Nadajemy uprawnienia (częsty problem na macOS/Linux)
RUN chmod +x mvnw

# Pobieramy zależności
RUN ./mvnw dependency:go-offline

COPY src ./src

CMD ["./mvnw", "spring-boot:run"]