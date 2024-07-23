package com.reactivepractice.common;

import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.user.handler.response.UserResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public class SessionUtils {

    public static final String USER_SESSION_KEY = "sessionedUser";

    public static Mono<UserResponse> getLoginUser(ServerRequest serverRequest){
        return serverRequest.session()
                .flatMap(webSession -> Mono.just((UserResponse) webSession.getAttribute(USER_SESSION_KEY)))
                .onErrorResume(NullPointerException.class, throwable -> Mono.error(new UnauthorizedException()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedException())));
    }

}
