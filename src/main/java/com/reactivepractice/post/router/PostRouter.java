package com.reactivepractice.post.router;

import com.reactivepractice.exception.model.CustomBaseException;
import com.reactivepractice.exception.handler.ExceptionHandler;
import com.reactivepractice.post.hadler.PostHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Slf4j
public class PostRouter {

    private final ExceptionHandler exceptionHandler;

    @Bean
    public RouterFunction<ServerResponse> routePosts(PostHandler postHandler){
        return RouterFunctions.route()
                .path("/posts", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), builder2 -> builder2
                        .POST("", postHandler::register)
                        .GET("", postHandler::getAllPosts)
                        .PATCH("", postHandler::modify)
                        .GET("/{id}", postHandler::getPost)
                        .DELETE("/{id}", postHandler::delete))
                )
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(CustomBaseException.class, exceptionHandler::handleGlobalException))
                .build();
    }

}
