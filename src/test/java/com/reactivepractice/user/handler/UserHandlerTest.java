package com.reactivepractice.user.handler;

import com.reactivepractice.exception.BadRequestException;
import com.reactivepractice.exception.DuplicationException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class UserHandlerTest {

    @Test
    @DisplayName("회원 등록")
    void register() {
        TestContainer testContainer = TestContainer.builder().build();
        UserRequest userRequest = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(userRequest));
        Mono<ServerResponse> register = testContainer.userHandler.register(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("회원 등록 실패")
    void failedRegister() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        UserRequest userRequest = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(userRequest));
        Mono<ServerResponse> register = testContainer.userHandler.register(request);
        StepVerifier.create(register)
                .expectError(DuplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("이메일로 검색")
    void findByEmail() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("email", "test@test.test")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.findByEmail(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("이메일로 검색 실패 비어있는 검색어")
    void failedFindByEmailWhenEmptyEmail() {
        TestContainer testContainer = TestContainer.builder().build();
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("email", "")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.findByEmail(request);
        StepVerifier.create(register)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("이메일로 검색 실패 비어있는 query Param")
    void failedFindByEmailWhenNullParameter() {
        TestContainer testContainer = TestContainer.builder().build();
        MockServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.findByEmail(request);
        StepVerifier.create(register)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("목록 조회")
    void findAll() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.findAll(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

}