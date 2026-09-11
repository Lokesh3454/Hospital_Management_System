package com.hospital.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URI;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class HmsApplication {

    public static void main(String[] args) {
        normalizeCloudDatabaseEnvironment();
        SpringApplication.run(HmsApplication.class, args);
    }

    private static void normalizeCloudDatabaseEnvironment() {
        String rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("DATABASE_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("MYSQL_URL");
        }

        if (rawUrl != null && !rawUrl.isBlank()) {
            String trimmed = rawUrl.trim();
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
                        System.setProperty("spring.datasource.username", parts[0]);
                        System.setProperty("SPRING_DATASOURCE_USERNAME", parts[0]);
                        if (parts.length > 1) {
                            System.setProperty("spring.datasource.password", parts[1]);
                            System.setProperty("SPRING_DATASOURCE_PASSWORD", parts[1]);
                        }
                    }

                    boolean isSslRequired = host != null && (host.contains("tidbcloud") || host.contains("aiven"));
                    String sslParams = isSslRequired
                            ? "?sslMode=VERIFY_IDENTITY&serverTimezone=UTC"
                            : "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

                    String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + sslParams;

                    System.setProperty("spring.datasource.url", jdbcUrl);
                    System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
                    System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
                    System.out.println("[HMS Boot] Normalized Cloud MySQL URL: jdbc:mysql://" + host + ":" + port + "/" + db + " (SSL=" + isSslRequired + ")");
                } catch (Exception e) {
                    System.err.println("[HMS Boot] Could not parse cloud DB URL: " + e.getMessage());
                }
            } else if (!trimmed.startsWith("jdbc:")) {
                String jdbcUrl = "jdbc:" + trimmed;
                System.setProperty("spring.datasource.url", jdbcUrl);
                System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
                System.out.println("[HMS Boot] Prepended jdbc: prefix to URL: " + jdbcUrl);
            }
        }
    }
}

