package com.reactivepractice.post.hadler;

import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.post.hadler.port.PostService;
import com.reactivepractice.post.hadler.response.PostResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
