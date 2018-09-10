create table generic_prod_request (
  id int8 not null,
  created_at timestamp,
  generic_prod_request_id varchar(255),
  project_name varchar(255),
  actioned_by_id int8,
  project_id int8,
  primary key (id)
);

create table generic_prod_request_open_shift_resources (
  generic_prod_request_entity_id int8 not null,
  open_shift_resources_id int8 not null
);

alter table generic_prod_request_open_shift_resources
  add constraint UK_l0evmqdtl1fpb27xtb9il4tnu unique (open_shift_resources_id);

alter table generic_prod_request
  add constraint FKfu5aky57tcsi99d973qa5p766
foreign key (actioned_by_id)
references team_member;

alter table generic_prod_request
  add constraint FKe4p89qd8vmyek6yyxnba314hs
foreign key (project_id)
references project;

alter table generic_prod_request_open_shift_resources
  add constraint FKcq7bkoavd8swlsll9057bgobo
foreign key (open_shift_resources_id)
references open_shift_resources;

alter table generic_prod_request_open_shift_resources
  add constraint FKjarqfbmcsgftkw3xn1ihttvl0
foreign key (generic_prod_request_entity_id)
references generic_prod_request;
