package com.reactivepractice.post.hadler.port;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostService {
    Mono<Post> register(PostRequest request, Long userId);
    Mono<Post> getPost(Long postId);
    Flux<Post> getAllPosts();
    Mono<Post> modify(PostRequest request, Long userId);
}
