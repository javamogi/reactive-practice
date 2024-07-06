package com.reactivepractice.user.service;

import com.reactivepractice.mock.FakeUserRepository;
import com.reactivepractice.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        this.userService = UserServiceImpl.builder()
                .userRepository(fakeUserRepository)
                .build();

        fakeUserRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
    }

    @Test
    @DisplayName("등록")
    void register(){
        User user = User.builder()
                .email("test2@test.test")
                .password("test2")
                .build();
        Mono<User> register = userService.register(user);
        StepVerifier.create(register)
                .assertNext(u -> {
                    assertThat(u.getId()).isEqualTo(2);
                    assertThat(u.getEmail()).isEqualTo("test2@test.test");
                    assertThat(u.getPassword()).isEqualTo("test2");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("이메일 조회")
    void findByEmail(){
        Mono<User> user = userService.findByEmail("test@test.test");
        StepVerifier.create(user)
                .assertNext(u -> {
                    assertThat(u.getId()).isEqualTo(1);
                    assertThat(u.getEmail()).isEqualTo("test@test.test");
                    assertThat(u.getPassword()).isEqualTo("test");
                })
                .verifyComplete();
    }

}