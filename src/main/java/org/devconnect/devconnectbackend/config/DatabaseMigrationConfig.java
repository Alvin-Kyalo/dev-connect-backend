package org.devconnect.devconnectbackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    public CommandLineRunner fixDevIdConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // Fix dev_id constraint
                jdbcTemplate.execute("ALTER TABLE projects ALTER COLUMN dev_id DROP NOT NULL");
                System.out.println("✅ Successfully removed NOT NULL constraint from dev_id column");
            } catch (Exception e) {
                System.out.println("ℹ️ dev_id constraint: " + e.getMessage());
            }

            try {
                // Check if is_claimed column exists, if so fix it, otherwise ignore
                jdbcTemplate.execute("ALTER TABLE projects ALTER COLUMN is_claimed DROP NOT NULL");
                System.out.println("✅ Successfully removed NOT NULL constraint from is_claimed column");
            } catch (Exception e) {
                System.out.println("ℹ️ is_claimed constraint: " + e.getMessage());
            }

            try {
                // Drop is_claimed column if it exists (we don't use it in the model)
                jdbcTemplate.execute("ALTER TABLE projects DROP COLUMN IF EXISTS is_claimed");
                System.out.println("✅ Successfully dropped unused is_claimed column");
            } catch (Exception e) {
                System.out.println("ℹ️ is_claimed column drop: " + e.getMessage());
            }
        };
    }
}
