package com.reactivepractice.comment.service;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
import com.reactivepractice.comment.handler.port.CommentService;
import com.reactivepractice.comment.service.port.CommentRepository;
import com.reactivepractice.exception.model.ErrorCode;
import com.reactivepractice.exception.model.NotFoundException;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Builder
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public Mono<Comment> register(CommentRequest request, Long userId) {
        return userRepository.findById(userId)
                .flatMap(user ->
                    postRepository.findById(request.getPostId())
                            .flatMap(post -> commentRepository.save(Comment.from(request, user, post))
                                    .map(comment -> Comment.from(comment, user, post)))
                            .switchIfEmpty(Mono.error(new NotFoundException(ErrorCode.NOT_FOUND_POST))))
                .switchIfEmpty(Mono.error(new NotFoundException(ErrorCode.NOT_FOUND_USER)));
    }
}
