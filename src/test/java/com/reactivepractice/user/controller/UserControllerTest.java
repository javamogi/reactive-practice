package com.reactivepractice.user.controller;

import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UserControllerTest {

    @Test
    @DisplayName("회원가입 성공")
    void successRegister(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        User user = User.builder()
                .email("test@test.test")
                .password("test")
                .build();

        // when
        Mono<ResponseEntity<User>> result = testContainer.userController.register(Mono.just(user));

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(1);
                    assertThat(response.getBody().getEmail()).isEqualTo("test@test.test");
                    assertThat(response.getBody().getPassword()).isEqualTo("test");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("회원가입 실패 가입된 이메일")
    void failedRegisterWhenDuplicateEmail(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        User user = User.builder()
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);

        // when
        Mono<ResponseEntity<User>> result = testContainer.userController.register(Mono.just(user));

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(409));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("이메일 검색 성공")
    void getUserByEmail(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        String email = "test@test.test";

        // when
        Mono<ResponseEntity<User>> result = testContainer.userController.getUserByEmail(Mono.just(email));

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(1);
                    assertThat(response.getBody().getEmail()).isEqualTo("test@test.test");
                    assertThat(response.getBody().getPassword()).isEqualTo("test");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("가입된 이메일 없음")
    void getUserByEmailWhenNotFound(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        String email = "test@test.test";

        // when
        Mono<ResponseEntity<User>> result = testContainer.userController.getUserByEmail(Mono.just(email));

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
                })
                .verifyComplete();
    }

}