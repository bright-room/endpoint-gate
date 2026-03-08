drop table if exists gate_management;

create table gate_management(
                                gate_id varchar(100) not null primary key,
    enabled boolean not null
);

insert into gate_management(gate_id, enabled)
values ('experimental', true),
       ('development', false);
