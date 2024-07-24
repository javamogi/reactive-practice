package com.reactivepractice.post.infrastructure;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PostReactiveRepository extends ReactiveCrudRepository<PostEntity, Long> {
}
