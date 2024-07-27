package com.reactivepractice.comment.handler;

import com.reactivepractice.comment.domain.CommentRequest;
import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.mock.TestContainer;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.user.domain.User;
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

class CommentHandlerTest {

    @Test
    @DisplayName("댓글 등록")
    void register(){
        // given
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .contents("내용")
                .user(user)
                .build();
        testContainer.postRepository.save(post);
        CommentRequest comment = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        // when
        Mono<ServerResponse> register = testContainer.commentHandler.register(request);

        // then
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 등록 실패 로그인 회원 없음")
    void failedRegisterWhenEmptyLoginUser(){
        // given
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        Post post = Post.builder()
                .id(1L)
                .title("제목")
                .contents("내용")
                .user(user)
                .build();
        testContainer.postRepository.save(post);
        CommentRequest comment = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(comment));

        // when
        Mono<ServerResponse> register = testContainer.commentHandler.register(request);

        // then
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("댓글 등록 실패 등록된 게시글 없음")
    void failedRegisterWhenEmptyPost(){
        // given
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        CommentRequest comment = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        // when
        Mono<ServerResponse> register = testContainer.commentHandler.register(request);

        // then
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

    @Test
    @DisplayName("댓글 등록 실패 가입하지 않은 회원")
    void failedRegisterWhenNotFoundUser(){
        // given
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        CommentRequest comment = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY,
                UserResponse.of(User.builder().id(2L).build()));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        // when
        Mono<ServerResponse> register = testContainer.commentHandler.register(request);

        // then
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }
}