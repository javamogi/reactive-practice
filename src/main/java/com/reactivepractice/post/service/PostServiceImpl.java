package com.reactivepractice.post.service;

import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.post.hadler.port.PostService;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Builder
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Mono<Post> register(PostRequest request, Long userId) {
        return userRepository.findById(userId)
                .flatMap(user ->
                    postRepository.save(Post.from(request, user))
                            .flatMap(p -> Mono.just(p.from(user)))
                )
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }

    @Override
    public Mono<Post> getPost(Long postId) {
        return postRepository.findById(postId)
                .flatMap(p -> userRepository.findById(p.getUser().getId())
                                .map(p::from))
                .switchIfEmpty(Mono.error(new NotFoundException()));
    }
}
