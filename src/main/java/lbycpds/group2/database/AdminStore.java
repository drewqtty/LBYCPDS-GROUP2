package lbycpds.group2.database;

import java.util.List;

public interface AdminStore {
    void saveAdmin(String name);

    List<String> loadAdmins();
}