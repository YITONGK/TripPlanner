package com.example.tripplanner.db;

import com.example.tripplanner.utils.DatabaseInterface;

import java.util.Map;

public class DatabaseContext {
    private DatabaseInterface database;

    public DatabaseContext(DatabaseInterface database) {
        this.database = database;
    }

    public void setDatabase(DatabaseInterface database) {
        this.database = database;
    }

    public void insert(String table, Map<String, Object> values) {
        database.insert(table, values);
    }


}

