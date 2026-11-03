package lbycpds.group2.database;

import java.sql.Connection;

public abstract class BaseDao {
    protected Connection connection;

    public BaseDao(Connection connection) {
        this.connection = connection;
    }
}