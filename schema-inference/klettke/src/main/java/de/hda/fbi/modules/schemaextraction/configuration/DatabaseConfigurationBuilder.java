package de.hda.fbi.modules.schemaextraction.configuration;

public class DatabaseConfigurationBuilder {

    private String _userName, _authDbName, _host, _port, _password;
    private boolean auth;

    public DatabaseConfigurationBuilder with(String _host, String _port) {
        this._host = _host;
        this._port = _port;
        this.auth = false;
        return this;
    }

    public DatabaseConfigurationBuilder with(String _host, String _port, String _authDbName, String _userName,
                                             String _password) {
        this._userName = _userName;
        this._authDbName = _authDbName;
        this._host = _host;
        this._port = _port;
        this._password = _password;
        this.auth = true;
        return this;
    }

    public DatabaseConfiguration build() {

        if (this.auth) {
            return new DatabaseConfiguration(_host, _port, _authDbName, _userName, _password);
        } else {
            return new DatabaseConfiguration(_host, _port);
        }
    }
}
