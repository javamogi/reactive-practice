package com.reactivepractice.comment.handler;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
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

    @Test
    @DisplayName("댓글 하나 조회")
    void getComment(){
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
        testContainer.commentRepository.save(Comment.builder()
                        .post(post)
                        .writer(user)
                        .contents("댓글 등록")
                .build());

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> register = testContainer.commentHandler.getComment(request);
        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("존재하지 않는 댓글")
    void getCommentWhenEmptyComment(){
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
                .pathVariable("id", "99")
                .build();
        Mono<ServerResponse> register = testContainer.commentHandler.getComment(request);
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회")
    void getComments(){
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
        testContainer.commentRepository.save(Comment.builder()
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .queryParam("postId", "1")
                .build();
        Mono<ServerResponse> comments = testContainer.commentHandler.getCommentsByPostId(request);
        StepVerifier.create(comments)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 빈 parameter")
    void failedGetCommentsWhenEmptyParameter(){
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
        testContainer.commentRepository.save(Comment.builder()
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .queryParam("postId", "")
                .build();
        Mono<ServerResponse> comments = testContainer.commentHandler.getCommentsByPostId(request);
        StepVerifier.create(comments)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException
                        && throwable.getMessage().equals("BAD_REQUEST"))
                .verify();
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회 문자열 parameter")
    void failedGetCommentsWhenStringParameter(){
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
        testContainer.commentRepository.save(Comment.builder()
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .queryParam("postId", "a")
                .build();
        Mono<ServerResponse> comments = testContainer.commentHandler.getCommentsByPostId(request);
        StepVerifier.create(comments)
                .expectErrorMatches(throwable -> throwable instanceof NumberFormatException)
                .verify();
    }

    @Test
    @DisplayName("댓글 수정")
    void modify(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        Post post = Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build();
        testContainer.postRepository.save(post);
        testContainer.commentRepository.save(Comment.builder()
                .id(1L)
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        CommentRequest comment = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        Mono<ServerResponse> register = testContainer.commentHandler.modify(request);

        StepVerifier.create(register)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("로그인하지 않은 회원은 댓글을 수정할 수 없다.")
    void failedModifyWhenNotLogin(){
        TestContainer testContainer = TestContainer.builder().build();
        CommentRequest comment = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(comment));

        Mono<ServerResponse> register = testContainer.commentHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("작성자가 다르면 댓글을 수정할 수 없다.")
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
        Post post = Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build();
        testContainer.postRepository.save(post);
        testContainer.commentRepository.save(Comment.builder()
                .id(1L)
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        CommentRequest comment = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();

        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user2));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        Mono<ServerResponse> register = testContainer.commentHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("존재하지 않는 회원은 댓글을 수정할 수 없다.")
    void failedModifyWhenEmptyUser(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        Post post = Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build();
        testContainer.postRepository.save(post);
        testContainer.commentRepository.save(Comment.builder()
                .id(1L)
                .post(post)
                .writer(user)
                .contents("댓글 등록")
                .build());

        CommentRequest comment = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        Mono<ServerResponse> register = testContainer.commentHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정할 수 없다.")
    void failedModifyWhenEmptyPost(){
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        CommentRequest comment = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        MockWebSession mockWebSession = new MockWebSession();
        mockWebSession.getAttributes().put(SessionUtils.USER_SESSION_KEY, UserResponse.of(user));
        MockServerRequest request = MockServerRequest.builder()
                .session(mockWebSession)
                .body(Mono.just(comment));

        Mono<ServerResponse> register = testContainer.commentHandler.modify(request);

        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제")
    void delete() {
        TestContainer testContainer = TestContainer.builder().build();
        User user = User.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .build();
        testContainer.userRepository.save(user);
        Post post = Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build();
        testContainer.postRepository.save(post);
        testContainer.commentRepository.save(Comment.builder()
                .id(1L)
                .post(post)
                .writer(user)
                .contents("댓글 등록")
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
        Mono<ServerResponse> result = testContainer.commentHandler.delete(request);
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 삭제 실패 로그인 회원 없음")
    void failedDeleteWhenNotLogin() {
        TestContainer testContainer = TestContainer.builder().build();
        testContainer.userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "1")
                .build();
        Mono<ServerResponse> deleted = testContainer.commentHandler.delete(request);
        StepVerifier.create(deleted)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제 실패 권한 없음")
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
        Post post = Post.builder()
                .id(1L)
                .user(user)
                .title("제목")
                .contents("내용")
                .build();
        testContainer.postRepository.save(post);
        testContainer.commentRepository.save(Comment.builder()
                .id(1L)
                .post(post)
                .writer(user2)
                .contents("댓글 등록")
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
        Mono<ServerResponse> deleted = testContainer.commentHandler.delete(request);
        StepVerifier.create(deleted)
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제 실패 잘못된 파라미터")
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
        Mono<ServerResponse> result = testContainer.commentHandler.delete(request);
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제 실패 존재하지 않는 댓글")
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
        Mono<ServerResponse> result = testContainer.commentHandler.delete(request);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }
}