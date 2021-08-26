package de.hda.fbi.modules.schemaextraction.configuration;

public class DatabaseConfiguration {

    private String userName, authDbName, host, port, password;

    private boolean auth;

    public DatabaseConfiguration(String _host, String _port) {
        this.host = _host;
        this.port = _port;
        this.setAuth(false);
    }

    public DatabaseConfiguration(String _host, String _port, String _authDbName, String _userName, String _password) {

        this.host = _host;
        this.port = _port;

        if (!_authDbName.equals("") && !_userName.equals("") && !_password.equals("")
                && !_authDbName.equals("undefined") && !_userName.equals("undefined")
                && !_password.equals("undefined")) {
            this.authDbName = _authDbName;
            this.userName = _userName;
            this.password = _password;
            this.setAuth(true);
        }
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAuthDbName() {
        return authDbName;
    }

    public void setAuthDbName(String authDbName) {
        this.authDbName = authDbName;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
