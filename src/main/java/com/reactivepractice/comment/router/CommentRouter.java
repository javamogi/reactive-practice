package com.reactivepractice.comment.router;

import com.reactivepractice.comment.handler.CommentHandler;
import com.reactivepractice.exception.CustomBaseException;
import com.reactivepractice.exception.ErrorResponse;
import com.reactivepractice.post.hadler.PostHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class CommentRouter {

    @Bean
    public RouterFunction<ServerResponse> routeComments(CommentHandler commentHandler){
        return RouterFunctions.route()
                .path("/comments", builder -> builder
                        .nest(accept(MediaType.APPLICATION_JSON), builder2 -> builder2
                        .POST("", commentHandler::register))
                )
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(CustomBaseException.class, this::handleGlobalException))
                .build();
    }

    private Mono<ServerResponse> handleGlobalException(CustomBaseException ex) {
        log.error("Global exception", ex);
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());

        return ServerResponse.status(ex.getErrorCode().getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

}
