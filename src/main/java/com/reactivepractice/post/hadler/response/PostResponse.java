package com.reactivepractice.post.hadler.response;

import com.reactivepractice.comment.handler.response.CommentResponse;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.handler.response.UserResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private List<CommentResponse> comments;

    public static PostResponse fromWithWriter(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContents())
                .writer(UserResponse.of(post.getUser()))
                .comments(Objects.nonNull(post.getComments()) ? post.getComments().stream().map(CommentResponse::fromWithoutPost).toList() : new ArrayList<>())
                .build();
    }

    public static PostResponse fromWithoutWriter(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContents())
                .build();
    }
}
