package com.reactivepractice.post.hadler;

import com.reactivepractice.common.SessionUtils;
import com.reactivepractice.exception.model.BadRequestException;
import com.reactivepractice.exception.model.ForbiddenException;
import com.reactivepractice.exception.model.NotFoundException;
import com.reactivepractice.exception.model.UnauthorizedException;
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

    @Test
    @DisplayName("게시글 하나 조회")
    void getPost(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        testContainer.postRepository.save(Post.builder()
                        .title("제목")
                        .contents("내용")
                        .user(user)
                .build());
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> register = testContainer.postHandler.getPost(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 없음")
    void getNotFoundPost(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> register = testContainer.postHandler.getPost(request);
        StepVerifier.create(register)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("로그인하지 않은 회원 게시글 조회")
    void getPostWhenNotLogin(){
        TestContainer testContainer = TestContainer.builder().build();
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> register = testContainer.postHandler.getPost(request);
        StepVerifier.create(register)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    @DisplayName("게시물 목록 조회")
    void getAll() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.postRepository.save(Post.builder()
                        .title("제목")
                        .contents("내용")
                        .user(User.builder().id(1L).build())
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> register = testContainer.postHandler.getAllPosts(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 수정")
    void modify(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        testContainer.postRepository.save(Post.builder()
                        .id(1L)
                        .user(user)
                        .title("제목")
                        .contents("내용")
                .build());
        PostRequest post = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(post));

        Mono<ServerResponse> register = testContainer.postHandler.modify(request);

        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("로그인하지 않은 회원은 게시글을 수정할 수 없다.")
    void failedModifyWhenNotLogin(){
        TestContainer testContainer = TestContainer.builder().build();
        PostRequest post = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(post));

        Mono<ServerResponse> register = testContainer.postHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("작성자가 다르면 게시글을 수정할 수 없다.")
    void failedModifyWhenNotMatchWriter(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        User user2 = User.builder()
                .id(2L)
                .email("test2@test.test")
                .password("test2")
                .build();
        testContainer.userRepository.save(user2);
        testContainer.postRepository.save(Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build());
        PostRequest post = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user2));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(post));

        Mono<ServerResponse> register = testContainer.postHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("존재하지 않는 회원은 게시글을 수정할 수 없다.")
    void failedModifyWhenEmptyUser(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.postRepository.save(Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build());
        PostRequest post = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(post));

        Mono<ServerResponse> register = testContainer.postHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정할 수 없다.")
    void failedModifyWhenEmptyPost(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        PostRequest post = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(post));

        Mono<ServerResponse> register = testContainer.postHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제")
    void delete() {
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        testContainer.postRepository.save(Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build());
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.test")
                .name("테스트")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, userResponse);
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .session(mockWebSession)
                .build();
        Mono<ServerResponse> result = testContainer.postHandler.delete(request);
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 삭제 실패 로그인 회원 없음")
    void failedDeleteWhenNotLogin() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> deleted = testContainer.postHandler.delete(request);
        StepVerifier.create(deleted)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제 실패 권한 없음")
    void failedDeleteNotMatchUser() {
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        User user2 = User.builder()
                .id(2L)
                .email("test2@test.test")
                .password("test2")
                .build();
        testContainer.userRepository.save(user);
        testContainer.postRepository.save(Post.builder()
                .id(1L)
                .user(user2)
                .title("제목")
                .contents("내용")
                .build());
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.test")
                .name("테스트")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, userResponse);
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .session(mockWebSession)
                .build();
        Mono<ServerResponse> deleted = testContainer.postHandler.delete(request);
        StepVerifier.create(deleted)
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제 실패 잘못된 파라미터")
    void failedDeleteWhenBadRequest() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.test")
                .name("테스트")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, userResponse);
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "")
                .session(mockWebSession)
                .build();
        Mono<ServerResponse> result = testContainer.postHandler.delete(request);
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제 실패 존재하지 않는 게시글")
    void failedDeleteWhenNotFoundPost() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@test.test")
                .name("테스트")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, userResponse);
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .session(mockWebSession)
                .build();
        Mono<ServerResponse> result = testContainer.postHandler.delete(request);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

}