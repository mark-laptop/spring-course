package ru.geekbrains.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.sqlite.SQLiteDataSource;

@Configuration
@ComponentScan(basePackages = "ru.geekbrains")
@PropertySource("classpath:db.properties")
public class Config {

    @Value(value = "${db_url}")
    private String dbURL;

    @Bean
    public SQLiteDataSource getDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(dbURL);
        return dataSource;
    }
}
