create table additional_environment (
                                        id int8 not null,
                                        display_name varchar(255),
                                        owning_project int8,
                                        primary key (id)
);

create table project_additional_environment_entities (
                                                         project_entity_id int8 not null,
                                                         additional_environment_entities_id int8 not null
);

alter table additional_environment
    add constraint UK87pc6jbt2fnpja7x3e8nl09hq unique (display_name, owning_project);

alter table project_additional_environment_entities
    add constraint UK_2p3p4p6h3slhx7iaeh2jj34vg unique (additional_environment_entities_id);

alter table additional_environment
    add constraint FKdgt7n9t58t2sak3fplti36lhv
        foreign key (owning_project)
            references project;

alter table project_additional_environment_entities
    add constraint FKkh62x076qurfrxvnpj9vbd4oq
        foreign key (additional_environment_entities_id)
            references additional_environment;

alter table project_additional_environment_entities
    add constraint FKlsxn13k618lwk29xg9yx3u0xl
        foreign key (project_entity_id)
            references project;