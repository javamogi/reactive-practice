package com.reactivepractice.comment.handler.port;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
import reactor.core.publisher.Mono;

public interface CommentService {
    Mono<Comment> register(CommentRequest request, Long userId);
}
