package com.reactivepractice.post.hadler.response;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.handler.response.UserResponse;
import lombok.*;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private UserResponse writer;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContents())
                .writer(UserResponse.of(post.getUser()))
                .build();
    }
}
