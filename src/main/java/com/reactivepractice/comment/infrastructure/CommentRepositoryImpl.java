package com.reactivepractice.comment.infrastructure;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.service.port.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentReactiveRepository commentReactiveRepository;

    @Override
    public Mono<Comment> save(Comment comment) {
        return commentReactiveRepository.save(CommentEntity.from(comment))
                .flatMap(c -> Mono.just(c.toModel()));
    }

    @Override
    public Mono<Void> deleteAll() {
        return commentReactiveRepository.deleteAll();
    }
}
