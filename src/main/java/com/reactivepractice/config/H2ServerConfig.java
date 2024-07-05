package com.reactivepractice.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class H2ServerConfig {
    @Value("${h2.console.port}")
    private Integer port;
    private Server webServer;

    private final ConnectionFactory connectionFactory;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
        log.info("started h2 console at port {}.", port);
        this.webServer = Server.createWebServer("-webPort", port.toString()).start();
    }

    private void initializeDatabase() {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(populator);
        initializer.afterPropertiesSet();
        log.info("Database initialized");
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        log.info("stopped h2 console at port {}.", port);
        this.webServer.stop();
    }
}
