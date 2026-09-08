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
import java.net.URI;

/**
 * Robust DataSource Configuration that automatically handles cloud database URLs
 * (such as Railway or Render format: mysql://user:pass@host:port/database)
 * and normalizes them into proper JDBC URLs for the MySQL Driver and HikariCP pool.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private static final String DEFAULT_URL = "jdbc:mysql://hayabusa.proxy.rlwy.net:40465/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "gZGpCIshoBVxfPzuThKBkqFClWNfNEWM";

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

        // Fallback to environment variables if properties are empty
        if (url == null || url.isBlank()) {
            url = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (url == null || url.isBlank()) {
            url = System.getenv("DATABASE_URL");
        }
        if (url == null || url.isBlank()) {
            url = System.getenv("MYSQL_URL");
        }

        // Process and normalize URL if provided in standard cloud URI format
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
                            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                    log.info("Successfully converted cloud MySQL URL to JDBC format: jdbc:mysql://{}:{}/{}", host, port, db);
                } catch (Exception e) {
                    log.warn("Failed to parse cloud datasource URL as URI: {}. Falling back to default Railway URL.", e.getMessage());
                    url = DEFAULT_URL;
                }
            } else if (!trimmed.startsWith("jdbc:")) {
                url = "jdbc:" + trimmed;
            }
        }

        if (url == null || url.isBlank() || !url.startsWith("jdbc:mysql://")) {
            url = DEFAULT_URL;
        }
        if (username == null || username.isBlank()) {
            username = DEFAULT_USERNAME;
        }
        if (password == null || password.isBlank()) {
            password = DEFAULT_PASSWORD;
        }

        log.info("Initializing HikariCP DataSource with target database host: {}", extractHost(url));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName != null && !driverClassName.isBlank() ? driverClassName : "com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setConnectionTimeout(20000);
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    private String extractHost(String jdbcUrl) {
        try {
            int start = jdbcUrl.indexOf("://");
            if (start != -1) {
                int end = jdbcUrl.indexOf(":", start + 3);
                if (end != -1) {
                    return jdbcUrl.substring(start + 3, end);
                }
            }
        } catch (Exception ignored) {}
        return "railway-cloud";
    }
}
