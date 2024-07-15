package com.reactivepractice.user.service;

import com.reactivepractice.exception.DuplicationException;
import com.reactivepractice.mock.FakeUserRepository;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        this.userService = UserServiceImpl.builder()
                .userRepository(fakeUserRepository)
                .build();

        fakeUserRepository.save(User.from(UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build()));
        fakeUserRepository.save(User.from(UserRequest.builder()
                .email("test2@test.test")
                .password("test2")
                .build()));
    }

    @Test
    @DisplayName("등록")
    void register() throws InterruptedException {
        //given
        Hooks.onOperatorDebug();
        UserRequest user = UserRequest.builder()
                .email("test3@test.test")
                .password("test3")
                .build();

        //when
        Mono<UserResponse> register = userService.register(user);

        //then
//        StepVerifier.create(userService.register(user))
        StepVerifier.create(register)
                .assertNext(u -> {
                    assertThat(u.getId()).isEqualTo(3);
                    assertThat(u.getEmail()).isEqualTo("test3@test.test");
                })
//                .expectNextMatches(userResponse -> userResponse.getEmail().equals("test3@test.test"))
                .verifyComplete();
    }

    @Test
    @DisplayName("등록 동시성 테스트")
    void registerParallel() throws InterruptedException {
        //given
        Hooks.onOperatorDebug();
        UserRequest user = UserRequest.builder()
                .email("test3@test.test")
                .password("test3")
                .build();

        UserRequest user2 = UserRequest.builder()
                .email("test3@test.test")
                .password("test3")
                .build();

        //when
        Mono<UserResponse> register = userService.register(user);
        Mono<UserResponse> register2 = userService.register(user2);
        List<Mono<UserResponse>> registers = List.of(register, register2);

        //then
        StepVerifier.create(Mono.when(registers))
                .expectError(DuplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("등록 실패 가입된 이메일")
    void failedRegisterWhenDuplicateEmail(){
        //given
        UserRequest user = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        //when
        Mono<UserResponse> register = userService.register(user);

        //then
        StepVerifier.create(register)
                .expectError(DuplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("이메일 조회")
    void findByEmail(){
        //given
        //when
        Mono<UserResponse> user = userService.findByEmail("test@test.test");

        //then
        StepVerifier.create(user)
                .assertNext(u -> {
                    assertThat(u.getId()).isEqualTo(1);
                    assertThat(u.getEmail()).isEqualTo("test@test.test");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("이메일 조회 정보 없음")
    void findByEmailWhenNotFound(){
        //given
        //when
        Mono<UserResponse> user = userService.findByEmail("test99@test.test");

        //then
        StepVerifier.create(user)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("회원 목록 조회")
    void findAll(){
        //given
        //when
        Flux<UserResponse> users = userService.findAll();

        //then
        StepVerifier.create(users)
                .expectNextCount(2)
                .assertNext(u -> assertThat(u.getEmail()).isEqualTo("test@test.test"))
                .assertNext(u -> assertThat(u.getEmail()).isEqualTo("test2@test.test"));
    }

}