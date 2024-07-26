package com.reactivepractice.post.service.port;

import com.reactivepractice.post.doamin.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository {

    Mono<Post> save(Post post);

    Mono<Post> findById(Long id);

    Flux<Post> findAll();

    Mono<Void> deleteById(Long id);

    Mono<Void> deleteALl();
}
