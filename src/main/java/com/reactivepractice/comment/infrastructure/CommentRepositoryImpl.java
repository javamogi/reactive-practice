package com.reactivepractice.comment.infrastructure;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.service.port.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentReactiveRepository commentReactiveRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Comment> save(Comment comment) {
        return commentReactiveRepository.save(CommentEntity.from(comment))
                .flatMap(c -> Mono.just(c.toModel()));
    }

    @Override
    public Mono<Comment> findById(Long id) {
        String sql = "SELECT c.*, u.id, u.name, u.email, p.id, p.title " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "JOIN posts p ON c.post_id = p.id " +
                "WHERE c.id = :id";
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(Comment::fromWithPost)
                .one();
    }

    @Override
    public Flux<Comment> findByPostId(Long postId) {
        String sql = "SELECT c.*, u.id, u.name, u.email " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.post_id = :postId";
        return databaseClient.sql(sql)
                .bind("postId", postId)
                .map(Comment::fromWithoutPost)
                .all();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return commentReactiveRepository.deleteById(id);
    }
}
