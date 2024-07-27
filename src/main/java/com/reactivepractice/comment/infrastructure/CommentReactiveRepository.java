package com.reactivepractice.comment.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CommentReactiveRepository extends ReactiveCrudRepository<CommentEntity, Long> {
}
