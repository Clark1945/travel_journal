package com.example.travel_journal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Autowired
    private Dotenv dotenv;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .url("jdbc:postgresql://localhost:5432/travel_journal")
                .username(dotenv.get("POSTGRES_USER"))
                .password(dotenv.get("POSTGRES_PASSWORD"))
                .driverClassName("org.postgresql.Driver")
                .build();
    }
} 