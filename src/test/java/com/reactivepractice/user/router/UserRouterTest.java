package com.reactivepractice.user.router;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.exception.ErrorCode;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.service.port.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeAll
    static void init(@Autowired UserRepository userRepository,
                     @Autowired PasswordEncoder passwordEncoder){
        userRepository.save(User.builder()
                .email("test@test.test")
                .password(passwordEncoder.encode("test"))
                .name("테스트")
                .build()).subscribe();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void register(){
        UserRequest request = UserRequest.builder()
                .email("test2@test.test")
                .password("test2")
                .name("테스트2")
                .build();
        webTestClient
                .post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class).value(user -> {
//                    assertThat(user.getId()).isEqualTo(2);
                    assertThat(user.getEmail()).isEqualTo("test2@test.test");
                    assertThat(user.getName()).isEqualTo("테스트2");
                });
    }

    @Test
    @DisplayName("회원 가입 실패 중복된 이메일")
    void failedRegister(){
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .name("테스트")
                .build();

        webTestClient
                .post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("이메일로 회원 찾기")
    void findByEmail(){
        String request = "test@test.test";
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("email", request)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("이메일로 회원 찾기 이메일 입력값 없음")
    void findByEmailWhenEmptyEmail(){
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("회원 목록")
    void findAll(){
        webTestClient
                .get().uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("로그인")
    void login(){
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("로그인 실패 가입되지 않은 이메일")
    void failedLoginWhenNotFoundUser(){
        LoginRequest request = LoginRequest.builder()
                .email("test99@test.test")
                .password("test")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(NotFoundException.class).value(ex -> {
                   assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                   assertThat(ex.getMessage()).isEqualTo(ErrorCode.NOT_FOUND.getMessage());
                });
    }

    @Test
    @DisplayName("로그인 실패 비밀번호 틀림")
    void failedLoginWhenNotMatchingPassword(){
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test2")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(UnauthorizedException.class).value(ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                    assertThat(ex.getMessage()).isEqualTo(ErrorCode.UNAUTHORIZED.getMessage());
                });
    }

    @Test
    @DisplayName("로그인 회원 정보")
    void getLoginUserInfo() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri("/users/login/info")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("로그인 회원 없음")
    void notLoginUser() {
        webTestClient
                .get().uri("/users/login/info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("로그아웃")
    void logout() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri("/users/logout")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk();

        webTestClient
                .get().uri("/users/login/info")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("삭제할 session 없음")
    void logoutWhenEmptySession() {
        webTestClient
                .get().uri("/users/logout")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("회원 정보 수정")
    void modify() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        UserRequest userRequest = UserRequest.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .name("이름수정")
                .build();
        webTestClient
                .patch().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                    assertThat(user.getName()).isEqualTo("이름수정");
                });
    }

    @Test
    @DisplayName("회원 정보 수정 실패 로그인한 회원 없음")
    void failedModifyWhenEmptyLoginUser() {
        UserRequest userRequest = UserRequest.builder()
                .id(1L)
                .email("test@test.test")
                .password("test")
                .name("이름수정")
                .build();
        webTestClient
                .patch().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("회원 정보 수정 실패 로그인한 회원과 수정 요청 회원 정보가 다름")
    void failedModifyWhenNotMatchUser() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        UserRequest userRequest = UserRequest.builder()
                .id(2L)
                .email("test2@test.test")
                .password("test2")
                .name("이름수정")
                .build();
        webTestClient
                .patch().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("회원 삭제")
    void delete() {

        Long id = userRepository.save(User.builder()
                .email("test10@test.test")
                .password(passwordEncoder.encode("test10"))
                .name("테스트10")
                .build())
                .map(User::getId).block();

        LoginRequest request = LoginRequest.builder()
                .email("test10@test.test")
                .password("test10")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/users/{id}")
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("회원 삭제 실패 로그인한 회원 없음")
    void failedDeleteWhenEmptyLoginUser() {
        long id = 1;
        webTestClient
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/users/{id}")
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("회원 삭제 실패 잘못된 path variable 형식")
    void failedDeleteWhenBadRequest() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/users/{id}")
                        .build("a"))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("회원 삭제 실패 권한 없음")
    void failedDeleteWhenNotMatchUser() {
        LoginRequest request = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        long id = 2;
        webTestClient
                .delete().uri(uriBuilder -> uriBuilder
                        .path("/users/{id}")
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isForbidden();
    }
}
