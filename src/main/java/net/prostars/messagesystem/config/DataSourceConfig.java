package net.prostars.messagesystem.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MySQL 연결에 사용할 DataSource를 구성한다.
 *
 * application.yml의 spring.datasource.hikari 설정을 읽어
 * HikariCP 기반 Connection Pool을 생성한다.
 */
@Configuration
public class DataSourceConfig {

    /**
     * 애플리케이션에서 사용할 DataSource Bean을 등록한다.
     */
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();

    }
}
