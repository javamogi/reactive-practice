package com.reactivepractice.post.hadler;

import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.BadRequestException;
import com.reactivepractice.exception.ForbiddenException;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.post.hadler.port.PostService;
import com.reactivepractice.post.hadler.response.PostResponse;
import com.reactivepractice.user.handler.response.UserResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Builder
public class PostHandler {

    private final PostService postService;

    public Mono<ServerResponse> register(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> request.bodyToMono(PostRequest.class)
                            .flatMap(p -> postService.register(p, user.getId())))
                .flatMap(p -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .body(BodyInserters.fromValue(PostResponse.from(p))));
    }

    public Mono<ServerResponse> getPost(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> Mono.just(request.pathVariable("id"))
                        .map(Long::parseLong)
                        .onErrorResume(NumberFormatException.class, throwable -> Mono.error(new BadRequestException()))
                        .flatMap(postService::getPost)
                        .flatMap(post -> ServerResponse.ok().body(BodyInserters.fromValue(PostResponse.from(post)))));
    }

    public Mono<ServerResponse> getAllPosts(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(postService.getAllPosts().map(PostResponse::from), PostResponse.class);
    }

    public Mono<ServerResponse> modify(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> request.bodyToMono(PostRequest.class)
                        .flatMap(p -> postService.modify(p, user.getId())))
                .flatMap(p -> ServerResponse
                        .status(HttpStatus.OK)
                        .body(BodyInserters.fromValue(PostResponse.from(p))));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return SessionUtils.getLoginUser(request)
                .flatMap(user -> Mono.just(request.pathVariable("id"))
                        .map(Long::parseLong)
                        .onErrorResume(NumberFormatException.class, throwable -> Mono.error(new BadRequestException()))
                        .flatMap((id -> postService.delete(id, user.getId())))
                .then(Mono.defer(() -> ServerResponse.noContent().build())));
    }
}
