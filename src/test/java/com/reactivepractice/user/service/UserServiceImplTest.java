package com.reactivepractice.user.service;

import com.reactivepractice.mock.FakeUserRepository;
import com.reactivepractice.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        Hooks.onOperatorDebug();
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
    @DisplayName("등록 실패 가입된 이메일")
    void failedRegisterWhenDuplicateEmail(){
        User user = User.builder()
                .email("test@test.test")
                .password("test")
                .build();
//        assertThrows(DuplicateKeyException.class,
//                () -> userService.register(user));
        Mono<User> register = userService.register(user);
        register.subscribe(u -> log.info("user : {}", u));
        StepVerifier.create(register)
                .expectError(DuplicateKeyException.class)
                .verify();
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

    @Test
    @DisplayName("이메일 조회 정보 없음")
    void findByEmailWhenNotFound(){
        Mono<User> user = userService.findByEmail("test2@test.test");
        StepVerifier.create(user)
                .expectNextCount(0)
                .verifyComplete();
    }

}