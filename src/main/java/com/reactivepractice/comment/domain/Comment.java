package com.reactivepractice.comment.domain;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class Comment {
    private Long id;
    private String contents;
    private User writer;
    private Post post;

    public static Comment from(CommentRequest request, User user, Post post) {
        return Comment.builder()
                .id(request.getId())
                .writer(user)
                .post(post)
                .contents(request.getComment())
                .build();
    }

    public static Comment from(Comment comment, User user, Post post) {
        return Comment.builder()
                .id(comment.getId())
                .writer(user)
                .post(post)
                .contents(comment.getContents())
                .build();
    }
}
