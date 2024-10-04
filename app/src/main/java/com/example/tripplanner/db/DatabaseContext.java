package com.example.tripplanner.db;

import java.util.Map;

public class DatabaseContext {
    private DatabaseInterface database;

    public DatabaseContext(DatabaseInterface database) {
        this.database = database;
    }

    public void setDatabase(DatabaseInterface database) {
        this.database = database;
    }

    public String insert(String table, Map<String, Object> values) {
        return database.insert(table, values);
    }


}

