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
//        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        return databaseClient.sql(sql)
                .bind("id", id)
//                .bind(0, id)
//                .map(row -> Post.builder()
//                        .id((Long) row.get("id"))
//                        .title((String) row.get("title"))
//                        .contents((String) row.get("contents"))
//                        .user(User.builder()
//                                .id((Long) row.get("user_id"))
//                                .email((String) row.get("email"))
//                                .name((String) row.get("name"))
//                                .build())
//                        .build())
                .map(row -> Post.from(row))
                .one();
    }

//    @Override
//    public Mono<Post> findById(Long id) {
//        return postReactiveRepository.findByIdForNativeQuery(id)
//                .flatMap(p -> Mono.just(p.toModel()));
//    }

    @Override
    public Flux<Post> findAll() {
//        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id";
        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id";
        return databaseClient.sql(sql)
//                .fetch().all()
//                .map(row -> Post.builder()
//                        .id((Long) row.get("id"))
//                        .title((String) row.get("title"))
//                        .contents((String) row.get("contents"))
//                        .user(User.builder()
//                                .id((Long) row.get("user_id"))
//                                .email((String) row.get("email"))
//                                .name((String) row.get("name"))
//                                .build())
//                        .build()
//                )
                .map(row -> Post.from(row))
                .all();
    }

    @Override
    public Mono<Void> deleteALl() {
        return postReactiveRepository.deleteAll();
    }

}
