package net.enelson.sopli.lib.database;

import java.sql.SQLException;

public interface SqlConsumer<T> {
    void accept(T value) throws SQLException;
}
