package com.reactivepractice.comment.router;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
import com.reactivepractice.comment.handler.response.CommentResponse;
import com.reactivepractice.comment.service.port.CommentRepository;
import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.hadler.response.PostResponse;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.service.port.UserRepository;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ScriptUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class CommentRouterTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    PostRepository postRepository;

    @Autowired
    ConnectionFactory connectionFactory;

    private void executeScriptBlocking(final Resource sqlScript) {
        Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        ScriptUtils.executeSqlScript(connection, sqlScript)
                                .doFinally(signalType -> {
                                    // Close the connection and handle potential errors
                                    Mono.from(connection.close()).subscribe(
                                            null,  // On success, do nothing
                                            error -> log.error("Error closing connection: " + error.getMessage())
                                    );
                                })
                )
                .doOnSuccess(unused -> log.info("Script executed successfully"))
                .doOnError(error -> log.error("Error executing script: " + error.getMessage()))
                .block();
    }

    @BeforeEach
    void rollOutTestData(@Value("classpath:/sql/comment-router-test-data.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @AfterEach
    void cleanUpTestData(@Value("classpath:/sql/delete-all-data.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @Test
    @DisplayName("댓글 등록")
    void register() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];


        CommentRequest commentRequest = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록2")
                .build();
        webTestClient
                .post().uri("/comments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(commentRequest)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentResponse.class).value(comment -> {
                    assertThat(comment.getId()).isEqualTo(2L);
                    assertThat(comment.getContent()).isEqualTo("댓글 등록2");
//                    assertThat(comment.getPost().getTitle()).isEqualTo("제목");
                    assertThat(comment.getWriter().getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("댓글 등록 실패 로그인한 회원 없음")
    void failedRegisterWhenNotLoginUser() {
        CommentRequest commentRequest = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록2")
                .build();
        webTestClient
                .post().uri("/comments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(commentRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("댓글 등록 실패 등록되지 않은 게시글")
    void failedRegisterWhenNotFoundPost() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];


        CommentRequest commentRequest = CommentRequest.builder()
                .postId(3L)
                .comment("댓글 등록2")
                .build();
        // webtestclient errormessage확인 테스트
        webTestClient
                .post().uri("/comments")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(commentRequest)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("댓글 조회")
    void getComment() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/comments/{id}")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CommentResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(1);
                    assertThat(post.getContent()).isEqualTo("댓글 등록");
                    assertThat(post.getWriter().getId()).isEqualTo(1);
                    assertThat(post.getWriter().getEmail()).isEqualTo("test@test.test");
                    assertThat(post.getWriter().getName()).isEqualTo("테스트");
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 조회")
    void getCommentWhenEmpty() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/comments/{id}")
                        .build(99))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("댓글 조회 로그인하지 않음")
    void getCommentWhenNotLogin() {
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/comments/{id}")
                        .build(2))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회")
    void getComments() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/comments")
                        .queryParam("postId", 1)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CommentResponse.class)
                .hasSize(1);
    }

}