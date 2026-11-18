package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminDao extends BaseDao implements AdminStore {

    public AdminDao(Connection connection) {
        super(connection);
        createTable();
    }

    // The admins table is created the first time
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS admins ("
                    + "name TEXT PRIMARY KEY, "
                    + "created_at TIMESTAMPTZ NOT NULL DEFAULT now(), "
                    + "last_login TIMESTAMPTZ NOT NULL DEFAULT now())");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the admins table: " + e.getMessage());
        }
    }

    public void saveAdmin(String name) {
        update("INSERT INTO admins (name) VALUES (?) "
                        + "ON CONFLICT (name) DO UPDATE SET last_login = now()",
                name);
    }

    public List<String> loadAdmins() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM admins ORDER BY name";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                names.add(result.getString("name"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load admins: " + e.getMessage());
        }
        return names;
    }
}