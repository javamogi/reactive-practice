package com.reactivepractice.post.infrastructure;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.service.port.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostReactiveRepository postReactiveRepository;

    @Override
    public Mono<Post> save(Post post) {
        return postReactiveRepository.save(PostEntity.from(post))
                .flatMap(p -> Mono.just(p.toModel()));
    }

    @Override
    public Mono<Post> findById(Long id) {
        return postReactiveRepository.findById(id)
                .flatMap(p -> Mono.just(p.toModel()));
    }

    @Override
    public Mono<Void> deleteALl() {
        return postReactiveRepository.deleteAll();
    }

}
