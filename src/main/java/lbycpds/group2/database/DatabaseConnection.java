package lbycpds.group2.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private String url;
    private String user;
    private String password;

    public DatabaseConnection() {
        this("db.properties");
    }

    public DatabaseConnection(String configPath) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(configPath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + configPath
                    + ". Copy db.properties.example to db.properties and fill it in.");
        }
        url = properties.getProperty("db.url");
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}