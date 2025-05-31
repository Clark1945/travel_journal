package com.example.travel_journal.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    private Dotenv dotenv;

    @Bean
    public Dotenv getDotenv() {
        return Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    @PostConstruct
    public void init() {
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // 設置環境變數
        setEnvVariable(dotenv, "GOOGLE_CLIENT_ID");
        setEnvVariable(dotenv, "GOOGLE_CLIENT_SECRET");
        setEnvVariable(dotenv, "JWT_SECRET");
    }

    private void setEnvVariable(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null) {
            System.setProperty(key, value);
        }
    }
} 