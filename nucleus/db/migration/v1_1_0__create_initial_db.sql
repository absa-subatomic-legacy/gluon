
create sequence hibernate_sequence start 1 increment 1;

create table application (
  id int8 not null,
  application_id varchar(255),
  application_type varchar(255),
  bitbucket_id varchar(255),
  bitbucket_repo_name varchar(255),
  remote_url varchar(255),
  repo_url varchar(255),
  slug varchar(255),
  created_at timestamp,
  description varchar(255),
  name varchar(255),
  created_by_id int8,
  project_id int8,
  primary key (id)
);

create table association_value_entry (
  id int8 not null,
  association_key varchar(255) not null,
  association_value varchar(255),
  saga_id varchar(255) not null,
  saga_type varchar(255),
  primary key (id)
);

create table bitbucket_project (
  id int8 not null,
  bitbucket_project_id varchar(255),
  created_at timestamp,
  description varchar(255),
  key varchar(255),
  name varchar(255),
  url varchar(255),
  created_by_id int8,
  primary key (id)
);

create table domain_event_entry (
  global_index int8 not null,
  event_identifier varchar(255) not null,
  meta_data oid,
  payload oid not null,
  payload_revision varchar(255),
  payload_type varchar(255) not null,
  time_stamp varchar(255) not null,
  aggregate_identifier varchar(255) not null,
  sequence_number int8 not null,
  type varchar(255),
  primary key (global_index)
);

create table membership_request (
  id int8 not null,
  membership_request_id varchar(255),
  request_status int4,
  team_id varchar(255),
  approved_by_id int8,
  requested_by_id int8,
  primary key (id)
);

create table pkg (
  id int8 not null,
  application_id varchar(255),
  created_at timestamp,
  description varchar(255),
  name varchar(255),
  package_type varchar(255),
  created_by_id int8,
  project_id int8,
  primary key (id)
);

create table project (
  id int8 not null,
  created_at timestamp,
  description varchar(255),
  name varchar(255),
  project_id varchar(255),
  bitbucket_project_id int8,
  created_by_id int8,
  owning_team_id int8,
  owning_tenant_id int8,
  primary key (id)
);

create table project_applications (
  project_entity_id int8 not null,
  applications_id int8 not null,
  primary key (project_entity_id, applications_id)
);

create table project_teams (
  project_entity_id int8 not null,
  teams_id int8 not null,
  primary key (project_entity_id, teams_id)
);

create table saga_entry (
  saga_id varchar(255) not null,
  revision varchar(255),
  saga_type varchar(255),
  serialized_saga oid,
  primary key (saga_id)
);

create table snapshot_event_entry (
  aggregate_identifier varchar(255) not null,
  sequence_number int8 not null,
  type varchar(255) not null,
  event_identifier varchar(255) not null,
  meta_data oid,
  payload oid not null,
  payload_revision varchar(255),
  payload_type varchar(255) not null,
  time_stamp varchar(255) not null,
  primary key (aggregate_identifier, sequence_number, type)
);

create table team (
  id int8 not null,
  created_at timestamp,
  description varchar(255),
  name varchar(255),
  team_channel varchar(255),
  team_id varchar(255),
  created_by_id int8,
  primary key (id)
);

create table team_member (
  id int8 not null,
  domain_username varchar(255),
  email varchar(255),
  first_name varchar(255),
  joined_at timestamp,
  last_name varchar(255),
  member_id varchar(255),
  screen_name varchar(255),
  user_id varchar(255),
  primary key (id)
);

create table team_member_teams (
  team_member_entity_id int8 not null,
  teams_id int8 not null,
  primary key (team_member_entity_id, teams_id)
);

create table team_members (
  team_entity_id int8 not null,
  members_id int8 not null,
  primary key (team_entity_id, members_id)
);

create table team_membership_requests (
  team_entity_id int8 not null,
  membership_requests_id int8 not null,
  primary key (team_entity_id, membership_requests_id)
);

create table team_owners (
  team_entity_id int8 not null,
  owners_id int8 not null,
  primary key (team_entity_id, owners_id)
);

create table tenant (
  id int8 not null,
  created_at timestamp,
  description varchar(255),
  name varchar(255),
  tenant_id varchar(255),
  primary key (id)
);

create table tenant_projects (
  tenant_entity_id int8 not null,
  projects_id int8 not null,
  primary key (tenant_entity_id, projects_id)
);

create table token_entry (
  processor_name varchar(255) not null,
  segment int4 not null,
  owner varchar(255),
  timestamp varchar(255) not null,
  token oid,
  token_type varchar(255),
  primary key (processor_name, segment)
);

create index IDXk45eqnxkgd8hpdn6xixn8sgft on association_value_entry (saga_type, association_key, association_value);

create index IDXgv5k1v2mh6frxuy5c0hgbau94 on association_value_entry (saga_id, saga_type);

alter table domain_event_entry
  add constraint UK8s1f994p4la2ipb13me2xqm1w unique (aggregate_identifier, sequence_number);

alter table domain_event_entry
  add constraint UK_fwe6lsa8bfo6hyas6ud3m8c7x unique (event_identifier);

alter table project_applications
  add constraint UK_i117fossikgg7wq1iwjns6qf3 unique (applications_id);

alter table snapshot_event_entry
  add constraint UK_e1uucjseo68gopmnd0vgdl44h unique (event_identifier);

alter table team_membership_requests
  add constraint UK_9mbewv9de6twv357ab1bw9xtv unique (membership_requests_id);

alter table tenant_projects
  add constraint UK_ad1kqxfo5ltxs1mp1ygnorvcm unique (projects_id);

alter table application
  add constraint FKnvu8eqvkrsjpvvojhklvs6yrm
foreign key (created_by_id)
references team_member;

alter table application
  add constraint FKrxh04lcvhpj4owpuk43oa0njh
foreign key (project_id)
references project;

alter table bitbucket_project
  add constraint FKbj8folxe7b8crd0hskncqo0it
foreign key (created_by_id)
references team_member;

alter table membership_request
  add constraint FK6phf6h17rl2a7m5o86wjqur30
foreign key (approved_by_id)
references team_member;

alter table membership_request
  add constraint FK2gvq2cpftoh0ventitrjtpo07
foreign key (requested_by_id)
references team_member;

alter table pkg
  add constraint FKerqll8uujl8d6vdds14g32q4k
foreign key (created_by_id)
references team_member;

alter table pkg
  add constraint FKaqbsre4b2watdnx85x2ewfn5q
foreign key (project_id)
references project;

alter table project
  add constraint FKnvvm0x6840p1rpb6mj96hh3kt
foreign key (bitbucket_project_id)
references bitbucket_project;

alter table project
  add constraint FK38ppb6fv8u1l8qhdkqy0jp9ct
foreign key (created_by_id)
references team_member;

alter table project
  add constraint FKdsogm9fls4kcjlfvsuaqk6um0
foreign key (owning_team_id)
references team;

alter table project
  add constraint FKpjh6l9sxo9dtsk8590sneau3a
foreign key (owning_tenant_id)
references tenant;

alter table project_applications
  add constraint FK1skgk1nqtacwoly4r4ljb5h5e
foreign key (applications_id)
references application;

alter table project_applications
  add constraint FKd2sd9p8isge5vsp07ulonc6jt
foreign key (project_entity_id)
references project;

alter table project_teams
  add constraint FK4wh13emifjt1w3o6nuny6ivh5
foreign key (teams_id)
references team;

alter table project_teams
  add constraint FK1a2epcac78838ykid9bx5pw8h
foreign key (project_entity_id)
references project;

alter table team
  add constraint FK7xjwdh6q290ady4hi4nyiu5w8
foreign key (created_by_id)
references team_member;

alter table team_member_teams
  add constraint FKe662inkr5ag9pteqsdpix1aba
foreign key (teams_id)
references team;

alter table team_member_teams
  add constraint FK18pxw38m15sftmjyvu8sc7fhv
foreign key (team_member_entity_id)
references team_member;

alter table team_members
  add constraint FK3d90dqttrv7mdpfxgdml4oljk
foreign key (members_id)
references team_member;

alter table team_members
  add constraint FKql6g4srg78r0n5uagxvtwlj12
foreign key (team_entity_id)
references team;

alter table team_membership_requests
  add constraint FK3n5jpl779x59bkh686ei4g1pk
foreign key (membership_requests_id)
references membership_request;

alter table team_membership_requests
  add constraint FKh9fpw1ulm944y76ass5ml25os
foreign key (team_entity_id)
references team;

alter table team_owners
  add constraint FKm5gm2lne8sohc3ulquxpiw1l
foreign key (owners_id)
references team_member;

alter table team_owners
  add constraint FKl0dohrfcg1a6qjtupr0dhr26e
foreign key (team_entity_id)
references team;

alter table tenant_projects
  add constraint FKd97nvbsq9wxdjn5vvsflbr3lv
foreign key (projects_id)
references project;

alter table tenant_projects
  add constraint FK37vw1xwdglebnevcsx88lk5wp
foreign key (tenant_entity_id)
references tenant
