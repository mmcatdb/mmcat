FROM maven:3.9.5-eclipse-temurin-21

WORKDIR /app

COPY . .

ARG serverPort
ARG serverOrigin
ARG serverOriginOld
ARG databasePassword

# We use /bin/echo because docker uses sh instead of bash where echo -e is not supported.
RUN /bin/echo -e "\n\
server.port=${serverPort}\n\
server.origin=${serverOrigin}\n\
server.originOld=${serverOriginOld}\n\
database.host=mmcat-database\n\
database.port=5432\n\
database.password=${databasePassword}\n\
setup.isInDocker=true" >> application.properties
# The configuration file has to be in the directory from which we run the `java` command. 

# TODO fix
# RUN mvn clean package -Dmaven.test.skip
RUN mvn clean package -DskipTests

# The file without -app is the original .jar file before repackage.
CMD [ "java", "-jar", "server/target/server-1.0-SNAPSHOT-app.jar" ]
