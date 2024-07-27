package com.reactivepractice.comment.service.port;

import com.reactivepractice.comment.domain.Comment;
import reactor.core.publisher.Mono;

public interface CommentRepository {
    Mono<Comment> save(Comment comment);
    Mono<Comment> findById(Long id);
}
