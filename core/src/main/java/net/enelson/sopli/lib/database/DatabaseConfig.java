package net.enelson.sopli.lib.database;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String poolName;
    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;
    private final Map<String, String> dataSourceProperties;

    private DatabaseConfig(Builder builder) {
        this.jdbcUrl = builder.jdbcUrl;
        this.username = builder.username;
        this.password = builder.password;
        this.poolName = builder.poolName;
        this.maximumPoolSize = builder.maximumPoolSize;
        this.minimumIdle = builder.minimumIdle;
        this.connectionTimeout = builder.connectionTimeout;
        this.idleTimeout = builder.idleTimeout;
        this.maxLifetime = builder.maxLifetime;
        this.dataSourceProperties = Collections.unmodifiableMap(new LinkedHashMap<String, String>(builder.dataSourceProperties));
    }

    public static Builder builder(String jdbcUrl) {
        return new Builder(jdbcUrl);
    }

    public static Builder mysql(String host, int port, String database) {
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&sslMode=DISABLED";
        return builder(jdbcUrl);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPoolName() {
        return poolName;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public Map<String, String> getDataSourceProperties() {
        return dataSourceProperties;
    }

    public static final class Builder {

        private final String jdbcUrl;
        private String username = "";
        private String password = "";
        private String poolName = "SopLibPool";
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30000L;
        private long idleTimeout = 600000L;
        private long maxLifetime = 1800000L;
        private final Map<String, String> dataSourceProperties = new LinkedHashMap<String, String>();

        private Builder(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public Builder credentials(String username, String password) {
            this.username = username == null ? "" : username;
            this.password = password == null ? "" : password;
            return this;
        }

        public Builder poolName(String poolName) {
            if (poolName != null && !poolName.trim().isEmpty()) {
                this.poolName = poolName;
            }
            return this;
        }

        public Builder maximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = Math.max(1, maximumPoolSize);
            return this;
        }

        public Builder minimumIdle(int minimumIdle) {
            this.minimumIdle = Math.max(0, minimumIdle);
            return this;
        }

        public Builder connectionTimeout(long connectionTimeout) {
            this.connectionTimeout = Math.max(250L, connectionTimeout);
            return this;
        }

        public Builder idleTimeout(long idleTimeout) {
            this.idleTimeout = Math.max(0L, idleTimeout);
            return this;
        }

        public Builder maxLifetime(long maxLifetime) {
            this.maxLifetime = Math.max(0L, maxLifetime);
            return this;
        }

        public Builder property(String key, String value) {
            if (key != null && !key.trim().isEmpty() && value != null) {
                this.dataSourceProperties.put(key, value);
            }
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
