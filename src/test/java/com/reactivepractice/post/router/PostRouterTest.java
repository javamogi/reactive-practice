package com.reactivepractice.post.router;

import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.post.hadler.response.PostResponse;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.handler.response.UserResponse;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
class PostRouterTest {

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
    void rollOutTestData(@Value("classpath:/sql/post-router-test-data.sql") Resource script) {
        executeScriptBlocking(script);
    }

    @AfterEach
    void cleanUpTestData(@Value("classpath:/sql/delete-all-data.sql") Resource script) {
        executeScriptBlocking(script);
    }


    @Autowired
    WebTestClient webTestClient;


    @Test
    @DisplayName("게시글 등록")
    void registerPost() {
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


        PostRequest postRequest = PostRequest.builder()
                .title("제목3")
                .contents("내용3")
                .build();
        webTestClient
                .post().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PostResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(3);
                    assertThat(post.getTitle()).isEqualTo("제목3");
                    assertThat(post.getContent()).isEqualTo("내용3");
                });
    }

    @Test
    @DisplayName("게시글 등록 실패 로그인 회원 없음")
    void failedRegisterPostWhenNotLogin() {

        PostRequest postRequest = PostRequest.builder()
                .title("제목")
                .contents("내용")
                .build();
        webTestClient
                .post().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 조회")
    void getPost() {
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
                        .path("/posts/{id}")
                        .build(2))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PostResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(2);
                    assertThat(post.getTitle()).isEqualTo("제목2");
                    assertThat(post.getContent()).isEqualTo("내용2");
                    assertThat(post.getWriter().getId()).isEqualTo(1);
                    assertThat(post.getWriter().getEmail()).isEqualTo("test@test.test");
                    assertThat(post.getWriter().getName()).isEqualTo("테스트");
                });
    }

    @Test
    @DisplayName("없는 게시글 조회")
    void getPostWhenEmpty() {
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
                        .path("/posts/{id}")
                        .build(99))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("게시글 조회 로그인하지 않음")
    void getPostWhenNotLogin() {
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(2))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void findAll(){
        webTestClient
                .get().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PostResponse.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("게시글 수정")
    void modify() {
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


        PostRequest postRequest = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        webTestClient
                .patch().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PostResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(1);
                    assertThat(post.getTitle()).isEqualTo("제목 수정");
                    assertThat(post.getContent()).isEqualTo("내용 수정");
                });
    }

    @Test
    @DisplayName("게시글 수정 실패 로그인 회원 없음")
    void failedModifyWhenNotLogin() {
        PostRequest postRequest = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        webTestClient
                .patch().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 수정 실패 존재하지 않는 게시글")
    void failedModify() {
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

        PostRequest postRequest = PostRequest.builder()
                .id(2L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        webTestClient
                .patch().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .header("SESSION", sessionId)
                .bodyValue(postRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 삭제")
    void delete() {
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
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("게시글글 삭제 실패 로그인 회원 없음")
    void failedDeleteWhenNotLogin() {
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 삭제 실패 권한 없음")
    void failedDeleteNotMatchUser() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test2@test.test")
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
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("게시글 삭제 실패 잘못된 파라미터")
    void failedDeleteWhenBadRequest() {
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
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build("a"))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("게시글 삭제 실패 존재하지 않는 게시글")
    void failedDeleteWhenNotFoundPost() {
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
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(99))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNotFound();
    }
}