package com.reactivepractice.post.hadler;

import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.post.doamin.PostRequest;
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
import static org.junit.jupiter.api.Assertions.*;

class PostHandlerTest {

    @Test
    @DisplayName("게시글 등록")
    void register(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        PostRequest post = PostRequest.builder()
                .title("제목")
                .contents("내용")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(post));
        Mono<ServerResponse> register = testContainer.postHandler.register(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 등록 실패 로그인 회원 없음")
    void failedRegisterWhenNotLogin(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        PostRequest post = PostRequest.builder()
                .title("제목")
                .contents("내용")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(post));
        Mono<ServerResponse> register = testContainer.postHandler.register(request);
        StepVerifier.create(register)
                .expectError(UnauthorizedException.class)
                .verify();
    }

}