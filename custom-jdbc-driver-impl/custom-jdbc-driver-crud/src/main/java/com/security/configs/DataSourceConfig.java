package com.security.configs;

import com.security.CustomJdbcDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private final String url;
    private final String user;
    private final String password;

    public DataSourceConfig(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String user,
            @Value("${spring.datasource.password}") String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driverManager = new DriverManagerDataSource();
        driverManager.setDriverClassName(CustomJdbcDriver.class.getName());
        driverManager.setUrl(url);
        driverManager.setUsername(user);
        driverManager.setPassword(password);
        return driverManager;
    }
}
