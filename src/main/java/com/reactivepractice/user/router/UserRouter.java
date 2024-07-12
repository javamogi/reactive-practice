package com.reactivepractice.user.router;

import com.reactivepractice.user.controller.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration(proxyBeanMethods = false)
public class UserRouter {

    @Bean
    public RouterFunction<ServerResponse> routeUsers(UserHandler userHandler){
        return RouterFunctions.route()
                .path("/users", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), builder2 -> builder2
                        .POST("", userHandler::register)
                        .GET("", userHandler::findAll))
                        .GET("/search", userHandler::findByEmail)
                )
                .build();
    }
}
