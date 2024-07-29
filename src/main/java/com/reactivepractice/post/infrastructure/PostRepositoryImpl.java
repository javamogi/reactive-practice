package com.reactivepractice.post.infrastructure;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostReactiveRepository postReactiveRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Post> save(Post post) {
        return postReactiveRepository.save(PostEntity.from(post))
                .flatMap(p -> Mono.just(p.toModel()));
    }

//    @Override
//    public Mono<Post> findById(Long id) {
//        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = :id";
////        String sql = "SELECT p.*, u.* , c.* " +
////                "FROM posts p " +
////                "JOIN users u ON p.user_id = u.id " +
////                "JOIN comments c ON c.post_id = p.id " +
////                "WHERE p.id = :id";
//        return databaseClient.sql(sql)
//                .bind("id", id)
//                .map(row -> Post.from(row))
//                .one();
//    }

//    @Override
//    public Mono<Post> findById(Long id) {
//        String sql = "SELECT p.id as post_id, p.title, p.contents, p.user_id, " +
//                "u.id as user_id, u.name, u.email, " +
//                "c.id as comment_id, c.contents as comment_contents, c.user_id as comment_user_id " +
//                "FROM posts p " +
//                "JOIN users u ON p.user_id = u.id " +
//                "LEFT JOIN comments c ON c.post_id = p.id " +
//                "WHERE p.id = :id";
//        return databaseClient.sql(sql)
//                .bind("id", id)
//                .fetch()
//                .all()
//                .bufferUntilChanged(result -> result.get("post_id"))
//                .map(rows ->
//                        Post.builder()
//                                .id((Long) rows.get(0).get("post_id"))
//                                .title((String) rows.get(0).get("title"))
//                                .contents((String) rows.get(0).get("contents"))
//                                .user(User.builder()
//                                        .id((Long) rows.get(0).get("user_id"))
//                                        .name((String) rows.get(0).get("name"))
//                                        .email((String) rows.get(0).get("email"))
//                                        .build())
//                                .comments(
//                                        rows.stream()
//                                                .map(row -> ((Long) row.get("comment_id")) != null ? Comment.builder()
//                                                        .id((Long) row.get("comment_id"))
//                                                        .contents((String) row.get("comment_contents"))
////                                                        .writer(User.builder()
////                                                                .id((Long))
////                                                                .build())
//                                                        .build() : null)
//                                                .toList()
//                                )
//                                .build()).single();
//    }

    @Override
    public Mono<Post> findById(Long id) {
        String sql = "SELECT p.id as post_id, p.title, p.contents, p.user_id, " +
                "u.id as p_user_id, u.name, u.email, " +
                "c.id as comment_id, c.contents as comment_contents, c.user_id, " +
                "cu.id as c_user_id, cu.name as cu_name " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN comments c ON c.post_id = p.id " +
                "LEFT JOIN users cu ON c.user_id = cu.id " +
                "WHERE p.id = :id";

        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .all()
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.empty();
                    }
                    Post post = null;
                    User user = null;
                    List<Comment> comments = null;

                    for (var row : rows) {
                        if (post == null) {
                            post = Post.builder()
                                    .id((Long) rows.get(0).get("post_id"))
                                    .title((String) rows.get(0).get("title"))
                                    .contents((String) rows.get(0).get("contents"))
                                    .user(User.builder()
                                            .id((Long) rows.get(0).get("p_user_id"))
                                            .name((String) rows.get(0).get("name"))
                                            .email((String) rows.get(0).get("email"))
                                            .build())
                                    .comments(comments)
                                    .build();
                            comments = new ArrayList<>();
                        }

                        if (row.get("comment_id") != null) {
                            comments.add(Comment.builder()
                                    .id((Long) row.get("comment_id"))
                                    .contents((String) row.get("comment_contents"))
                                    .writer(User.builder()
                                            .id((Long) row.get("c_user_id"))
                                            .name((String) row.get("cu_name"))
                                            .build())
                                    .build());
                        }
                    }

                    if (post != null) {
                        post.setComments(comments);
                    }
                    return Mono.just(post);
                });
    }

//    @Override
//    public Mono<Post> findById(Long id) {
//        String sql = "SELECT p.id as post_id, p.title, p.contents, p.user_id, " +
//                "u.id as user_id, u.name, u.email, " +
//                "c.id as comment_id, c.contents as comment_contents, c.user_id as comment_user_id " +
//                "FROM posts p " +
//                "JOIN users u ON p.user_id = u.id " +
//                "LEFT JOIN comments c ON c.post_id = p.id " +
//                "WHERE p.id = :id";
//        return databaseClient.sql(sql)
//                .bind("id", id)
//                .fetch()
//                .all()
//                .bufferUntilChanged(result -> result.get("post_id"))
//                .map(rows -> {
//                    List<Comment> comments = rows.stream()
//                            .map(row -> Comment.builder()
//                                    .id((Long) row.get("comment_id"))
//                                    .contents((String) row.get("comment_contents"))
////                                                        .writer(User.builder()
////                                                                .id((Long))
////                                                                .build())
//                                    .build())
//                            .toList();
//                    Map<String, Object> stringObjectMap = rows.get(0);
//                    return Post.builder()
//                            .id((Long) stringObjectMap.get("post_id"))
//                            .title((String) stringObjectMap.get("title"))
//                            .contents((String) stringObjectMap.get("contents"))
//                            .user(User.builder()
//                                    .id((Long) stringObjectMap.get("user_id"))
//                                    .name((String) stringObjectMap.get("name"))
//                                    .email((String) stringObjectMap.get("email"))
//                                    .build())
//                            .comments(comments)
//                            .build();
//                });
//    }

    @Override
    public Flux<Post> findAll() {
        String sql = "SELECT p.*, u.* FROM posts p JOIN users u ON p.user_id = u.id";
        return databaseClient.sql(sql)
                .map(row -> Post.from(row))
                .all();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return postReactiveRepository.deleteById(id);
    }

}
