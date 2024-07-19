package com.reactivepractice.user.handler;

import com.reactivepractice.exception.BadRequestException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.handler.port.UserService;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.domain.UserRequest;
import lombok.Builder;
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
@Builder
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> register(ServerRequest serverRequest){
        return serverRequest.bodyToMono(UserRequest.class)
                .flatMap(userService::register)
                .flatMap(response ->
                        ServerResponse.status(HttpStatus.CREATED).body(BodyInserters.fromValue(response)));
    }

    public Mono<ServerResponse> getUserByEmail(ServerRequest serverRequest) {
        return serverRequest.queryParam("email")
                .filter(email -> !email.isEmpty())
                .map(email -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(userService.findByEmail(email), UserResponse.class))
                .orElseGet(() -> Mono.error(new BadRequestException()));
    }

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("id"))
                .map(Long::parseLong)
                .onErrorResume(NumberFormatException.class, throwable -> Mono.error(new BadRequestException()))
                .flatMap(id -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(userService.findById(id), UserResponse.class)
                        .switchIfEmpty(Mono.error(new BadRequestException())));
    }

    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(userService.findAll(), UserResponse.class);
    }

    public Mono<ServerResponse> login(ServerRequest serverRequest){
        return serverRequest.bodyToMono(UserRequest.class)
                .flatMap(userService::login)
                .flatMap(user -> serverRequest.session()
                        .doOnNext(webSession -> webSession.getAttributes().put("user", user))
                        .flatMap(webSession -> ServerResponse.status(HttpStatus.OK)
                                .body(BodyInserters.fromValue(user))));
    }

    public Mono<ServerResponse> getLoginUser(ServerRequest serverRequest){
        return serverRequest.session()
                .flatMap(webSession -> Mono.just((UserResponse) webSession.getAttribute("user")))
                .onErrorResume(NullPointerException.class, throwable -> Mono.error(new UnauthorizedException()))
                .flatMap(user -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(user)));
    }

}
