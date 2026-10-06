FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package

FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/novatech-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
ENV CATALINA_OPTS="-Xms64m -Xmx256m -XX:MaxMetaspaceSize=128m"
EXPOSE 8080
CMD ["catalina.sh", "run"]
