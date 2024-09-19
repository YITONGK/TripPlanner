package com.example.tripplanner;

import java.util.List;
import java.util.Map;

public interface DatabaseInterface {
    void insert(String table, Map<String, Object> values);
    void update(String table, Map<String, Object> values, String whereClause);
    void delete(String table, String whereClause);
    List<Map<String, Object>> query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy);
}

