package com.reactivepractice.post.doamin;

import com.reactivepractice.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    private Long id;

    private User user;

    private String title;

    private String contents;

    public static Post from(PostRequest post, User user) {
        return Post.builder()
                .id(post.getId())
                .user(user)
                .title(post.getTitle())
                .contents(post.getContents())
                .build();
    }

    public Post from(User user) {
        return Post.builder()
                .id(id)
                .user(user)
                .title(title)
                .contents(contents)
                .build();
    }

}
