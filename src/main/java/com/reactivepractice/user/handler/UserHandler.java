package com.reactivepractice.user.handler;

import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.model.BadRequestException;
import com.reactivepractice.exception.model.ForbiddenException;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.handler.port.UserService;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.handler.response.UserResponse;
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
                        ServerResponse.status(HttpStatus.CREATED).body(BodyInserters.fromValue(UserResponse.of(response))));
    }

    public Mono<ServerResponse> getUserByEmail(ServerRequest serverRequest) {
        return serverRequest.queryParam("email")
                .filter(email -> !email.isEmpty())
                .map(email -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(userService.findByEmail(email).map(UserResponse::of), UserResponse.class))
                .orElseGet(() -> Mono.error(new BadRequestException()));
    }


    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        return getPathVariableId(serverRequest)
                .flatMap(id -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(userService.findById(id).map(UserResponse::of), UserResponse.class)
                        .switchIfEmpty(Mono.error(new BadRequestException())));
    }

    public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(userService.findAll().map(UserResponse::of), UserResponse.class);
    }

    public Mono<ServerResponse> login(ServerRequest serverRequest){
        return serverRequest.bodyToMono(LoginRequest.class)
                .flatMap(userService::login)
                .flatMap(user -> serverRequest.session()
                        .doOnNext(webSession -> webSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user)))
                        .flatMap(webSession -> ServerResponse.status(HttpStatus.OK)
                                .body(BodyInserters.fromValue(UserResponse.of(user)))));
    }

    public Mono<ServerResponse> getLoginUser(ServerRequest serverRequest){
        return SessionUtils.getLoginUser(serverRequest)
                .flatMap(user -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(user)));
    }

    public Mono<ServerResponse> logout(ServerRequest serverRequest){
        return SessionUtils.getLoginUser(serverRequest)
                .flatMap(user ->
                    serverRequest.session()
                            .flatMap(webSession -> {
                                webSession.invalidate().subscribe();
//                                webSession.getAttributes().remove("user");
                                return ServerResponse.ok().build();
                            }));
    }

    public Mono<ServerResponse> modify(ServerRequest serverRequest){
        return SessionUtils.getLoginUser(serverRequest)
                .flatMap(user -> serverRequest.bodyToMono(UserRequest.class)
                        .filter(request -> user.getId().equals(request.getId()))
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new ForbiddenException())))
                        .flatMap(userService::modify))
                .flatMap(response ->
                                ServerResponse.ok().body(BodyInserters.fromValue(UserResponse.of(response))));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest){
        return SessionUtils.getLoginUser(serverRequest)
                .flatMap(user -> getPathVariableId(serverRequest)
                        .filter(id -> user.getId().equals(id))
                        .switchIfEmpty(Mono.defer(() -> Mono.error(new ForbiddenException())))
                        .flatMap(userService::delete))
                .then(Mono.defer(() -> ServerResponse.noContent().build()));
    }

    private Mono<Long> getPathVariableId(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("id"))
                .map(Long::parseLong)
                .onErrorResume(NumberFormatException.class, throwable -> Mono.error(new BadRequestException()));
    }

}
