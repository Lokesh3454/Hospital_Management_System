package com.hospital.hms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

/**
 * Highly resilient DataSource Configuration that automatically detects and handles
 * local MySQL and cloud database URLs (Railway, Render, etc.).
 * Configures HikariCP with active TCP keepalives and connection validation
 * to completely prevent dropped connection issues and 'Could not open JPA EntityManager' errors.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private static final String DEFAULT_LOCAL_URL = "jdbc:mysql://localhost:3306/hospital_management_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true";
    private static final String DEFAULT_LOCAL_USERNAME = "root";
    private static final String DEFAULT_LOCAL_PASSWORD = "Lokesh@3454";

    private static final String DEFAULT_CLOUD_URL = "jdbc:mysql://hayabusa.proxy.rlwy.net:40465/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true";
    private static final String DEFAULT_CLOUD_USERNAME = "root";
    private static final String DEFAULT_CLOUD_PASSWORD = "gZGpCIshoBVxfPzuThKBkqFClWNfNEWM";

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = configuredUrl;
        String username = configuredUsername;
        String password = configuredPassword;

        // Priority 1: Check environment variables (e.g. Render, Railway, Docker)
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("DATABASE_URL");
        }
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("MYSQL_URL");
        }

        // Priority 2: Check if local MySQL (127.0.0.1:3306) is available
        boolean isLocalMySqlUp = isPortOpen("127.0.0.1", 3306, 1200);

        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl;
        } else if (isLocalMySqlUp && (configuredUrl == null || configuredUrl.isBlank() || configuredUrl.contains("localhost") || configuredUrl.contains("127.0.0.1"))) {
            log.info("Local MySQL is reachable on port 3306 and no cloud environment variable is set. Using local database.");
            url = DEFAULT_LOCAL_URL;
            username = (configuredUsername != null && !configuredUsername.isBlank() && !configuredUsername.equals("root")) ? configuredUsername : DEFAULT_LOCAL_USERNAME;
            password = (configuredPassword != null && !configuredPassword.isBlank() && !configuredPassword.equals("gZGpCIshoBVxfPzuThKBkqFClWNfNEWM")) ? configuredPassword : DEFAULT_LOCAL_PASSWORD;
        } else if (!isLocalMySqlUp) {
            log.info("Local MySQL is not running or running on cloud container (Render/Railway). Using Railway Cloud MySQL.");
            url = (configuredUrl != null && !configuredUrl.isBlank() && !configuredUrl.contains("localhost") && !configuredUrl.contains("127.0.0.1")) ? configuredUrl : DEFAULT_CLOUD_URL;
            username = (configuredUsername != null && !configuredUsername.isBlank() && !configuredUsername.equals("root")) ? configuredUsername : DEFAULT_CLOUD_USERNAME;
            password = (configuredPassword != null && !configuredPassword.isBlank() && !configuredPassword.equals("Lokesh@3454")) ? configuredPassword : DEFAULT_CLOUD_PASSWORD;
        }

        // Priority 3: Process and normalize cloud URI format (mysql://user:pass@host:port/db)
        if (url != null && !url.isBlank()) {
            String trimmed = url.trim();
            if (trimmed.startsWith("mysql://") || trimmed.startsWith("mysqls://") || (trimmed.startsWith("jdbc:mysql://") && trimmed.contains("@"))) {
                try {
                    String clean = trimmed.startsWith("jdbc:") ? trimmed.substring(5) : trimmed;
                    URI uri = URI.create(clean);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                    String path = uri.getPath();
                    String db = (path != null && path.length() > 1) ? path.substring(1) : "railway";

                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && !userInfo.isBlank()) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        if (parts.length > 1) {
                            password = parts[1];
                        }
                    }

                    url = "jdbc:mysql://" + host + ":" + port + "/" + db
                            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true";
                    log.info("Successfully converted cloud MySQL URL to JDBC format: jdbc:mysql://{}:{}/{}", host, port, db);
                } catch (Exception e) {
                    log.warn("Failed to parse cloud datasource URL as URI: {}. Falling back to standard URL handling.", e.getMessage());
                }
            } else if (!trimmed.startsWith("jdbc:")) {
                url = "jdbc:" + trimmed;
            }
        }

        // Ensure autoReconnect is present in URL
        if (url != null && !url.contains("autoReconnect")) {
            url += (url.contains("?") ? "&" : "?") + "autoReconnect=true";
        }

        log.info("Initializing HikariCP DataSource with target database: {}", extractHost(url));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName != null && !driverClassName.isBlank() ? driverClassName : "com.mysql.cj.jdbc.Driver");

        // Resilient connection pool settings to prevent cloud proxy disconnects
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(120000);       // 2 minutes
        config.setMaxLifetime(600000);        // 10 minutes (prevents cloud proxy silent TCP drops)
        config.setConnectionTimeout(20000);   // 20 seconds wait for connection
        config.setValidationTimeout(3000);    // 3 seconds validation timeout
        config.setKeepaliveTime(30000);       // 30 seconds keepalive ping (critical for Railway/cloud proxies)
        config.setConnectionTestQuery("SELECT 1");

        // High-performance MySQL connection flags
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("connectTimeout", "10000");
        config.addDataSourceProperty("socketTimeout", "30000");

        return new HikariDataSource(config);
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractHost(String jdbcUrl) {
        try {
            int start = jdbcUrl.indexOf("://");
            if (start != -1) {
                int end = jdbcUrl.indexOf(":", start + 3);
                if (end != -1) {
                    return jdbcUrl.substring(start + 3, end);
                }
                int slash = jdbcUrl.indexOf("/", start + 3);
                if (slash != -1) {
                    return jdbcUrl.substring(start + 3, slash);
                }
            }
        } catch (Exception ignored) {}
        return "database-host";
    }
}
