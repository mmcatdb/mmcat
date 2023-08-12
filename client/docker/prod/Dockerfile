# First stage - node

FROM node AS stage-build

# Set the working directory for all other instructions
WORKDIR /app

# Copy package*.json files from local filesystem to the working directory on image
# For caching purposes
COPY package*.json .

RUN npm install --loglevel verbose

# Copy the rest
COPY . .

RUN npm run build

# Second stage - nginx

FROM nginx

COPY --from=stage-build /app/dist /var/www/html

COPY docker/prod/nginx.conf /etc/nginx/
COPY docker/prod/app.conf /etc/nginx/sites-enabled/

EXPOSE 80

#CMD [ "tail", "-f", "/dev/null" ]
CMD [ "nginx", "-g", "daemon off;" ]