# Apache configuration

Both the backend and frontend application can run on the same server. To achieve this, you can use [this](apache-example.conf) example configuration. It works like this:
- All requests on the `/mmcat` path are routed to the frontend application, i.e. to the `/var/www/html/mmcat/example-ui/dist` directory.
- The static `.html`, `.js` etc. files that are here are served to the client.
- The frontend application fetches data on the `/mmcat-api` path.
- These requests are routed to the server localhost with the port `27500`.
- There listens the backend application which handles the requests.
