FROM maven:3.8.5-openjdk-17

WORKDIR /app

COPY . .

ARG serverPort
ARG serverOrigin
ARG databasePassword

RUN echo -e "\n\
server.port=${serverPort}\n\
server.origin=${serverOrigin}\n\
database.host=mmcat-database\n\
database.port=5432\n\
database.password=${databasePassword}\n\
setup.isInDocker=true" >> server/src/main/resources/application.properties

# TODO fix
# RUN mvn clean package -Dmaven.test.skip
RUN mvn clean package -DskipTests

# The file without -app is the original .jar file before repackage.
CMD [ "java", "-jar", "server/target/server-1.0-SNAPSHOT-app.jar" ]
