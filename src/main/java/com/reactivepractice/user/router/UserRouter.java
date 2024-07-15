package com.reactivepractice.user.router;

import com.reactivepractice.exception.CustomBaseException;
import com.reactivepractice.exception.ErrorResponse;
import com.reactivepractice.user.controller.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(CustomBaseException.class, this::handleGlobalException))
                .build();
    }

    private Mono<ServerResponse> handleGlobalException(CustomBaseException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());

        return ServerResponse.status(ex.getErrorCode().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}
