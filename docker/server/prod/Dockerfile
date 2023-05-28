FROM maven:3.8.5-openjdk-17

WORKDIR /app

COPY . .

RUN cp server/src/main/resources/application.properties.sample server/src/main/resources/application.properties

# RUN echo -e "\n\
# server.port=${SERVER_PORT}\n\
# server.origin=${EXAMPLE_UI_DOMAIN}\n\
# \n\
# postgresql.username=${DB_USERNAME}\n\
# postgresql.password=${DB_PASSWORD}\n\
# postgresql.password=${DB_DATABASE}" >> server/src/main/resources/application.properties

# TODO fix
# RUN mvn clean package -Dmaven.test.skip
RUN mvn clean package -DskipTests

ENTRYPOINT [ "docker/server/prod/entrypoint.sh" ]