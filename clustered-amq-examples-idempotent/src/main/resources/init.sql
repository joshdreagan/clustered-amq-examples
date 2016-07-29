CREATE SCHEMA SA;

CREATE TABLE messages (
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    dup_id varchar(255) NOT NULL,
    message varchar(4000) NOT NULL,
    constraint messages_pk PRIMARY KEY (id)
);

CREATE INDEX messages_dup_id_idx ON messages (dup_id);
