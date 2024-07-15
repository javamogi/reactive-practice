package com.reactivepractice.user.controller;

import com.reactivepractice.exception.BadRequestException;
import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> register(ServerRequest serverRequest){
        return serverRequest.bodyToMono(UserRequest.class)
                .flatMap(userService::register)
                .flatMap(response ->
                        ServerResponse.status(HttpStatus.CREATED).body(BodyInserters.fromValue(response)));
    }

    public Mono<ServerResponse> findByEmail(ServerRequest serverRequest) {
        return serverRequest.queryParam("email")
                .filter(email -> !email.isEmpty())
                .map(email -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(userService.findByEmail(email), UserResponse.class))
                .orElseGet(() -> Mono.error(new BadRequestException()));
    }

    public Mono<ServerResponse> findAll(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(userService.findAll(), UserResponse.class);
    }

}
