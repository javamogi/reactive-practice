create table if not exists users (
    id bigint not null auto_increment,
    email varchar(50) not null unique,
    password varchar(100) not null,
    name varchar(50) not null,
    primary key (id)
);
create table if not exists posts (
    id bigint not null auto_increment,
    user_id bigint not null,
    title varchar(100) not null,
    contents varchar(100) not null,
    primary key (id),
    constraint post_writer_fk FOREIGN KEY (user_id)
        REFERENCES users (id)
);