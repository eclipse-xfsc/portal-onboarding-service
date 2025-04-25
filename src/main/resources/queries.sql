create table organization (
    id serial,
    name varchar(50),
    email varchar(50) unique,
    aisbl boolean,
    registration_state varchar(50)
);

create table user_reg (
    id serial,
    firstname varchar(50),
    lastname varchar(50),
    email varchar(50) unique,
    phone_number varchar(20),
    street varchar(100),
    zip varchar(50),
    city varchar(50),
    country varchar(50),
    registration_state varchar(50)
);

create table requests_tbl (
    id serial,
    request_id varchar(50) unique null,
    request_type varchar(50),
    entity_id varchar(50) unique null,
    entity_type varchar(10) not null,
    name varchar(100) not null,
    reg_email varchar(50) unique,
    location varchar(50),
    json_spec jsonb not null
);

create table rq_attaches (
    id serial,
    email varchar(50),
    filename varchar(50),
    attach bytea
);

create user gaiax_ppr with encrypted password 'xzaq1W2e3r4$';
grant connect on database gaiax to gaiax_ppr;
grant select, update, insert on all tables in schema public to gaiax_ppr;
grant select, update on all sequences in schema public to gaiax_ppr;
