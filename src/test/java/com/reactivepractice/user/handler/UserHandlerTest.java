package com.reactivepractice.user.handler;

import com.reactivepractice.exception.BadRequestException;
import com.reactivepractice.exception.DuplicationException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.handler.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockWebSession;
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
    void getUserByEmail() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("email", "test@test.test")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getUserByEmail(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("이메일로 검색 실패 비어있는 검색어")
    void failedGetUserByEmailWhenEmptyEmail() {
        TestContainer testContainer = TestContainer.builder().build();
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("email", "")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getUserByEmail(request);
        StepVerifier.create(register)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("이메일로 검색 실패 비어있는 query Param")
    void failedGetUserByEmailWhenNullParameter() {
        TestContainer testContainer = TestContainer.builder().build();
        MockServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getUserByEmail(request);
        StepVerifier.create(register)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("ID로 검색")
    void getUserById() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getUserById(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("ID로 검색 빈 값")
    void getUserByIdWhenEmptyPathVariable() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "")
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getUserById(request);
        StepVerifier.create(register)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("목록 조회")
    void getAll() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> register = testContainer.userHandler.getAll(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("로그인")
    void login() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .session(new MockWebSession())
                .body(Mono.just(loginRequest));
        Mono<ServerResponse> login = testContainer.userHandler.login(request);
        StepVerifier.create(login)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("로그인 실패 가입하지 않은 회원")
    void failedLoginWhenNotFoundUser() {
        TestContainer testContainer = TestContainer.builder().build();
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(loginRequest));
        Mono<ServerResponse> login = testContainer.userHandler.login(request);
        StepVerifier.create(login)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("로그인 실패 비밀번호 틀림")
    void failedLoginWhenNotMatchingPassword() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test2")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(loginRequest));
        Mono<ServerResponse> login = testContainer.userHandler.login(request);
        StepVerifier.create(login)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    @DisplayName("session 회원")
    void session() {
        TestContainer testContainer = TestContainer.builder().build();
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.test")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put("user", userResponse);
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .build();
        Mono<ServerResponse> login = testContainer.userHandler.getLoginUser(request);
        StepVerifier.create(login)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

}