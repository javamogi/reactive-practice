delete from `posts` where 1;
delete from `users` where 1;

ALTER TABLE `users` ALTER COLUMN id RESTART WITH 1;
ALTER TABLE `posts` ALTER COLUMN id RESTART WITH 1;