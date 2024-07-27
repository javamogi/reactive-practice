package com.reactivepractice.comment.handler;

import com.reactivepractice.comment.domain.CommentRequest;
import com.reactivepractice.comment.handler.port.CommentService;
import com.reactivepractice.comment.handler.response.CommentResponse;
import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.model.BadRequestException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
@Builder
public class CommentHandler {

    private final CommentService commentService;

    public Mono<ServerResponse> register(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> request.bodyToMono(CommentRequest.class)
                        .flatMap(c -> commentService.register(c, user.getId())))
                .flatMap(c -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .body(BodyInserters.fromValue(CommentResponse.from(c))));
    }

    public Mono<ServerResponse> getComment(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> Mono.just(request.pathVariable("id"))
                        .map(Long::parseLong)
                        .onErrorResume(NumberFormatException.class, throwable -> Mono.error(new BadRequestException()))
                        .flatMap(commentService::getComment)
                        .flatMap(comment -> ServerResponse.ok().body(BodyInserters.fromValue(CommentResponse.from(comment)))));
    }
}
