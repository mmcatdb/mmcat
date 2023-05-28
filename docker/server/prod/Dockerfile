FROM maven:3.8.5-openjdk-17

WORKDIR /app

COPY . .

ARG serverPort
ARG serverOrigin
ARG postgresqlPassword

RUN cp server/src/main/resources/application.properties.sample server/src/main/resources/application.properties
RUN echo -e "\n\
server.port=${serverPort}\n\
server.origin=${serverOrigin}\n\
postgresql.password=${postgresqlPassword}" >> server/src/main/resources/application.properties

# TODO fix
# RUN mvn clean package -Dmaven.test.skip
RUN mvn clean package -DskipTests

CMD [ "java", "-jar", "server/target/server-1.0-SNAPSHOT.jar" ]
