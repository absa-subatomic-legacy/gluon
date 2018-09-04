create table project_prod_request (
  id int8 not null,
  approval_status int4,
  closed_at timestamp,
  created_at timestamp,
  project_name varchar(255),
  project_prod_request_id varchar(255),
  actioned_by_id int8,
  project_id int8,
  rejecting_member_id int8,
  primary key (id)
);

create table project_prod_request_authorizing_members (
  project_prod_request_entity_id int8 not null,
  authorizing_members_id int8 not null
);

alter table project_prod_request
  add constraint FKeo6pb6at5a263eup234rapuu
foreign key (actioned_by_id)
references team_member;

alter table project_prod_request
  add constraint FK7d64tgn9seemi4gbnodkgjg38
foreign key (project_id)
references project;

alter table project_prod_request
  add constraint FKs4uuqkpaeqmitroc2lajers0t
foreign key (rejecting_member_id)
references team_member;

alter table project_prod_request_authorizing_members
  add constraint FK3w07j51260xttn2in5njr1ek3
foreign key (authorizing_members_id)
references team_member;

alter table project_prod_request_authorizing_members
  add constraint FK6lgevrj03u9jk5hdk79pplhj6
foreign key (project_prod_request_entity_id)
references project_prod_request;