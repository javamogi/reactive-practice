package com.reactivepractice.user.infrastructure;

import com.reactivepractice.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}
