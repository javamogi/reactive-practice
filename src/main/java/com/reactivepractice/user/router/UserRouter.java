package com.reactivepractice.user.router;

import com.reactivepractice.common.BCryptPasswordEncoder;
import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.exception.model.CustomBaseException;
import com.reactivepractice.exception.handler.ExceptionHandler;
import com.reactivepractice.user.handler.UserHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Slf4j
public class UserRouter {

    private final ExceptionHandler exceptionHandler;

    @Bean
    public RouterFunction<ServerResponse> routeUsers(UserHandler userHandler){
        return RouterFunctions.route()
                .path("/users", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), builder2 -> builder2
                        .POST("", userHandler::register)
                        .GET("", userHandler::getAll)
                        .PATCH("", userHandler::modify)
                        .GET("/search", userHandler::getUserByEmail)
                        .POST("/login", userHandler::login)
                        .GET("/logout", userHandler::logout)
                        .GET("/login/info", userHandler::getLoginUser)
                        .DELETE("/{id}", userHandler::delete)
                        .GET("/{id}", userHandler::getUserById))
                )
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(CustomBaseException.class, exceptionHandler::handleGlobalException))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
