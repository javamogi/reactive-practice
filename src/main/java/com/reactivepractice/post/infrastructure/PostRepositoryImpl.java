package com.reactivepractice.post.infrastructure;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostReactiveRepository postReactiveRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Post> save(Post post) {
        return postReactiveRepository.save(PostEntity.from(post))
                .flatMap(p -> Mono.just(p.toModel()));
    }

    @Override
    public Mono<Post> findById(Long id) {
        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = :id";
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(row -> Post.from(row))
                .one();
    }

    @Override
    public Flux<Post> findAll() {
        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id";
        return databaseClient.sql(sql)
                .map(row -> Post.from(row))
                .all();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return postReactiveRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteALl() {
        return postReactiveRepository.deleteAll();
    }

}
