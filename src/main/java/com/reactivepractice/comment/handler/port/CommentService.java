package com.reactivepractice.comment.handler.port;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentService {
    Mono<Comment> register(CommentRequest request, Long userId);
    Mono<Comment> getComment(Long id);
    Flux<Comment> getCommentList(Long postId);
    Mono<Comment> modify(CommentRequest request, Long userId);
}
