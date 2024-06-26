# MM-cat

A set of tools for modeling, evolution and querying of multi-model data based on category theory. There are the following modules:
- [library](./library/README.md) - A java framework containing most of the algorithms and a web server.
- [client](./client-old/README.md) - A client part of the web application which serves as an UI for the whole framework.

## Configuration

There is a `.env.sample` file with basic variables. Copy it to `.env` and fill all missing ones. Then continue to the `library` and `client` directories and follow configuration guides there.

## Installation

- The application should be run via docker. First, create containers for the databases.
```bash
docker compose -f compose.db.prod.yaml up -d --build
```
- Then build the application.
```bash
docker compose -f compose.app.prod.yaml up -d --build
```

## Development

- There is another compose setup for development outside of docker.
```bash
docker compose -f compose.db.dev.yaml up -d --build
```
- Now you should be able to access [adminer](http://localhost:3203/adminer.php?pgsql=mmcat-database&username=mmcat&db=mmcat&ns=public) for the server database.
- Then follow installation guides in the `library` and `client` directories.
