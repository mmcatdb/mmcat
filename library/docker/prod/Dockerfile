FROM maven:3.9.5-eclipse-temurin-21

WORKDIR /app

COPY . .

ARG serverPort
ARG serverOrigin
ARG databasePassword

# Install gnupg to be able to get mongosh
RUN apt-get update && apt-get install -y curl gnupg python3 python3-venv

# Install mongosh; We need mongosh to re-execute Mongodb DML commands (see the execute(Path path) method in MongoDBControlWrapper)
RUN curl -fsSL https://pgp.mongodb.com/server-6.0.asc | gpg --dearmor -o /usr/share/keyrings/mongodb-server-6.0.gpg && \
    echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-6.0.gpg ] https://repo.mongodb.org/apt/debian bullseye/mongodb-org/6.0 main" | tee /etc/apt/sources.list.d/mongodb-org-6.0.list && \
    apt-get update && \
    apt-get install -y mongodb-mongosh && \
    rm -rf /var/lib/apt/lists/*

RUN cd aimm && \
    python3 -m venv venv && \
    . venv/bin/activate && \
    pip install -e .

# We use /bin/echo because docker uses sh instead of bash where echo -e is not supported.
RUN /bin/echo -e "\n\
server.port=${serverPort}\n\
server.origin=${serverOrigin}\n\
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