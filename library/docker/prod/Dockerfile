FROM maven:3.9.5-eclipse-temurin-21

WORKDIR /app

COPY . .

ARG serverPort
ARG serverOrigin
ARG serverOriginOld
ARG databasePassword

# Install gnupg to be able to get mongosh
RUN apt-get update && apt-get install -y curl gnupg

# Install mongosh; We need mongosh to re-execute Mongodb DML commands (see the execute(Path path) method in MongoDBControlWrapper)
RUN curl -fsSL https://pgp.mongodb.com/server-6.0.asc | gpg --dearmor -o /usr/share/keyrings/mongodb-server-6.0.gpg && \
    echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-6.0.gpg ] https://repo.mongodb.org/apt/debian bullseye/mongodb-org/6.0 main" | tee /etc/apt/sources.list.d/mongodb-org-6.0.list && \
    apt-get update && \
    apt-get install -y mongodb-mongosh && \
    rm -rf /var/lib/apt/lists/*

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
# CMD [ "java", "-jar", "server/target/server-1.0-SNAPSHOT-app.jar" ]
# Arguments needed in order to make apache spark work with java 21. Remove them as soon as we upgrade!
CMD ["java", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED", "--add-opens=java.base/java.net=ALL-UNNAMED", "--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED", "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED", "--add-opens=java.base/sun.security.action=ALL-UNNAMED", "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED", "--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED", "-Djava.security.manager=allow", "-jar", "server/target/server-1.0-SNAPSHOT-app.jar"]
