insert into users(email, password, name) values('test@test.test', '$2a$10$L2fDoGd9yu5IWexzAfhrCOQ6nwQxiW7KSFVbUuhxbCQlodW/4Ih2y', '테스트');
insert into users(email, password, name) values('test2@test.test', '$2a$10$L2fDoGd9yu5IWexzAfhrCOQ6nwQxiW7KSFVbUuhxbCQlodW/4Ih2y', '테스트2');
insert into posts(user_id, title, contents) values(1, '제목', '내용');
insert into comments(post_id, user_id, contents) values(1, 1, '댓글 등록');