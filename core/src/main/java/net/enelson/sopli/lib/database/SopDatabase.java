package net.enelson.sopli.lib.database;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SopDatabase implements AutoCloseable {

    private final HikariDataSource dataSource;
    private final Runnable closeHook;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    SopDatabase(HikariDataSource dataSource, Runnable closeHook) {
        this.dataSource = dataSource;
        this.closeHook = closeHook;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void execute(String sql) throws SQLException {
        withConnection(new SqlConsumer<Connection>() {
            @Override
            public void accept(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement(sql);
                    statement.execute();
                } finally {
                    closeQuietly(statement);
                }
            }
        });
    }

    public int executeUpdate(final String sql) throws SQLException {
        return withConnection(new SqlFunction<Connection, Integer>() {
            @Override
            public Integer apply(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement(sql);
                    return statement.executeUpdate();
                } finally {
                    closeQuietly(statement);
                }
            }
        });
    }

    public <T> T query(final String sql, final SqlFunction<ResultSet, T> mapper) throws SQLException {
        return withConnection(new SqlFunction<Connection, T>() {
            @Override
            public T apply(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = connection.prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    return mapper.apply(resultSet);
                } finally {
                    closeQuietly(resultSet);
                    closeQuietly(statement);
                }
            }
        });
    }

    public <T> T withConnection(SqlFunction<Connection, T> handler) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            return handler.apply(connection);
        } finally {
            closeQuietly(connection);
        }
    }

    public void withConnection(final SqlConsumer<Connection> handler) throws SQLException {
        withConnection(new SqlFunction<Connection, Void>() {
            @Override
            public Void apply(Connection connection) throws SQLException {
                handler.accept(connection);
                return null;
            }
        });
    }

    public <T> T transaction(SqlFunction<Connection, T> handler) throws SQLException {
        Connection connection = null;
        boolean autoCommit = true;
        try {
            connection = getConnection();
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            T result = handler.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException exception) {
            rollbackQuietly(connection);
            throw exception;
        } finally {
            restoreAutoCommit(connection, autoCommit);
            closeQuietly(connection);
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                dataSource.close();
            } finally {
                if (closeHook != null) {
                    closeHook.run();
                }
            }
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit(Connection connection, boolean autoCommit) {
        if (connection == null) {
            return;
        }
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }
}
