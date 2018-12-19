create table dev_deployment_environment (
  id int8 not null,
  display_name varchar(255),
  position_in_pipeline int4 not null,
  prefix varchar(255),
  pipeline_id int8,
  primary key (id)
);

create table dev_deployment_pipeline (
  id int8 not null,
  name varchar(255),
  pipeline_id varchar(255),
  primary key (id)
);

create table dev_deployment_pipeline_environments (
  dev_deployment_pipeline_entity_id int8 not null,
  environments_id int8 not null
);

create table project_release_deployment_pipelines (
  project_entity_id int8 not null,
  release_deployment_pipelines_id int8 not null
);

create table release_deployment_environment (
  id int8 not null,
  display_name varchar(255),
  position_in_pipeline int4 not null,
  prefix varchar(255),
  pipeline_id int8,
  primary key (id)
);

create table release_deployment_pipeline (
  id int8 not null,
  name varchar(255),
  pipeline_id varchar(255),
  tag varchar(255),
  primary key (id)
);

create table release_deployment_pipeline_environments (
  release_deployment_pipeline_entity_id int8 not null,
  environments_id int8 not null
);

alter table project add column dev_deployment_pipeline_id int8;

alter table application_prod_request add column deployment_pipeline_id int8;

alter table application_prod_request
  add constraint FKe3g40wxdvh85vpbfgeerm60fo
foreign key (deployment_pipeline_id)
references release_deployment_pipeline;

alter table generic_prod_request add column deployment_pipeline_id int8;

alter table generic_prod_request
  add constraint FKo47e32ackxjx09mp9hyefw1c7
foreign key (deployment_pipeline_id)
references release_deployment_pipeline;

alter table project_prod_request add column deployment_pipeline_id int8;

alter table project_prod_request
  add constraint FKr1b3y468bdoqe1iyyr3id9ll
foreign key (deployment_pipeline_id)
references release_deployment_pipeline;

alter table dev_deployment_pipeline_environments
  add constraint UK_j3vu971yw7kwavq3mwhdn000b unique (environments_id);

alter table project_release_deployment_pipelines
  add constraint UK_k88c9iqqwhgelo966a0wruvt1 unique (release_deployment_pipelines_id);

alter table release_deployment_pipeline_environments
  add constraint UK_fu8s8uakmhuwmh35uy5s5cs2q unique (environments_id);

alter table dev_deployment_environment
  add constraint FKq02qd18vpy1sfii0kltg5xbnf
foreign key (pipeline_id)
references dev_deployment_pipeline;

alter table dev_deployment_pipeline_environments
  add constraint FKrffevxjqi2fkq7tsujxc0xr8h
foreign key (environments_id)
references dev_deployment_environment;

alter table dev_deployment_pipeline_environments
  add constraint FK7gdld9noyyckgv67070v6dypq
foreign key (dev_deployment_pipeline_entity_id)
references dev_deployment_pipeline;

alter table project
  add constraint FK7rbfxjd92lt310j6opr1t2w9a
foreign key (dev_deployment_pipeline_id)
references dev_deployment_pipeline;

alter table project_release_deployment_pipelines
  add constraint FKg7kvwfll64c4gc68hgmbwpq8w
foreign key (release_deployment_pipelines_id)
references release_deployment_pipeline;

alter table project_release_deployment_pipelines
  add constraint FK555t1ff3465y0ayk7eufwqc0
foreign key (project_entity_id)
references project;

alter table release_deployment_environment
  add constraint FKe6ha803d9wolu5kftr2pa895u
foreign key (pipeline_id)
references release_deployment_pipeline;

alter table release_deployment_pipeline_environments
  add constraint FKaxb6ed4kdspds56v6jh75jpim
foreign key (environments_id)
references release_deployment_environment;

alter table release_deployment_pipeline_environments
  add constraint FKq3v5vuw2rf6dndyjns8f2witr
foreign key (release_deployment_pipeline_entity_id)
references release_deployment_pipeline;