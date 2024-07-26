package com.reactivepractice.post.doamin;

import com.reactivepractice.user.domain.User;
import lombok.*;
import io.r2dbc.spi.Readable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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

    public static Post from(Readable row){
        return Post.builder()
                .id((Long) row.get("id"))
                .title((String) row.get("title"))
                .contents((String) row.get("contents"))
                .user(User.builder()
                        .id((Long) row.get("user_id"))
                        .email((String) row.get("email"))
                        .name((String) row.get("name"))
                        .build())
                .build();
    }

    public boolean matchWriter(User user) {
        return this.user.getId().equals(user.getId());
    }
}
