create table metadata (
       id int8 not null,
        description varchar(255),
        primary key (id)
    );

create table metadata_metadata_entries (
       metadata_entity_id int8 not null,
        metadata_entries_id int8 not null
    );

create table metadata_entries (
       id int8 not null,
        key varchar(255),
        value varchar(255),
        primary key (id)
    );

create table team_metadata (
       team_entity_id int8 not null,
        metadata_id int8 not null
    );

alter table metadata_metadata_entries
       add constraint UK_bj8pf59ufh2v31gial7dttldp unique (metadata_entries_id);

alter table team_metadata
       add constraint UK_dewvn4wj01bq0qbt5j97ykt5 unique (metadata_id);

alter table metadata_metadata_entries
       add constraint FKl2y5gkplj8mwancpskp7efph
       foreign key (metadata_entries_id)
       references metadata_entries;

alter table metadata_metadata_entries
       add constraint FKsfvtja09akw678opntjh2j34s
       foreign key (metadata_entity_id)
       references metadata;

alter table team_metadata
       add constraint FKmjx0hk35ksgek47hn5lgfm64q
       foreign key (metadata_id)
       references metadata;

alter table team_metadata
       add constraint FKp1aw81ube3vdpmbt8igcjkhjo
       foreign key (team_entity_id)
       references team;
