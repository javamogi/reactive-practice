package com.reactivepractice.user.controller;

import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UserControllerTest {

    @Test
    @DisplayName("회원가입 성공")
    void successRegister(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        // when
        Mono<ResponseEntity<UserResponse>> result = testContainer.userController.register(Mono.just(request));

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(1);
                    assertThat(response.getBody().getEmail()).isEqualTo("test@test.test");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("회원가입 실패 가입된 이메일")
    void failedRegisterWhenDuplicateEmail(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        UserRequest user = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(User.from(user));

        // when
        Mono<ResponseEntity<UserResponse>> result = testContainer.userController.register(Mono.just(user));

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
        testContainer.userRepository.save(User.from(UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build()));
        String email = "test@test.test";

        // when
        Mono<ResponseEntity<UserResponse>> result = testContainer.userController.getUserByEmail(email);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().getId()).isEqualTo(1);
                    assertThat(response.getBody().getEmail()).isEqualTo("test@test.test");
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
        Mono<ResponseEntity<UserResponse>> result = testContainer.userController.getUserByEmail(email);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("회원 목록 조회")
    void getUsers(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        testContainer.userRepository.save(User.from(UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build()));
        testContainer.userRepository.save(User.from(UserRequest.builder()
                .email("test2@test.test")
                .password("test2")
                .build()));

        // when
        Mono<ResponseEntity<List<UserResponse>>> result = testContainer.userController.getUsers();

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("목록 조회 없음")
    void getUsersEmpty(){
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();

        // when
        Mono<ResponseEntity<List<UserResponse>>> result = testContainer.userController.getUsers();

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody()).isEmpty();
                })
                .verifyComplete();
    }

}