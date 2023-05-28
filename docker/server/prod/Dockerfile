FROM maven:3.8.5-openjdk-17

WORKDIR /app

COPY . .

RUN cp server/src/main/resources/application.properties.sample server/src/main/resources/application.properties

# TODO fix
# RUN mvn clean package -Dmaven.test.skip
RUN mvn clean package -DskipTests

RUN chmod +x docker/server/prod/entrypoint.sh
ENTRYPOINT [ "docker/server/prod/entrypoint.sh" ]