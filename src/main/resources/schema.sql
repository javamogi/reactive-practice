create table if not exists users (
    id bigint not null auto_increment,
    email varchar(50) not null,
    password varchar(50) not null,
    primary key (id)
);