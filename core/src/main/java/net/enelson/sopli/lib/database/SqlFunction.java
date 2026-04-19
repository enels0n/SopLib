package net.enelson.sopli.lib.database;

import java.sql.SQLException;

public interface SqlFunction<T, R> {
    R apply(T value) throws SQLException;
}
