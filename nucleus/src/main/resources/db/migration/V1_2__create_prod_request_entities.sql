create table application_prod_request (
  id int8 not null,
  application_name varchar(255),
  application_prod_request_id varchar(255),
  created_at timestamp,
  project_name varchar(255),
  actioned_by_id int8,
  application_id int8,
  primary key (id)
);

create table application_prod_request_open_shift_resources (
  application_prod_request_entity_id int8 not null,
  open_shift_resources_id int8 not null
);

create table open_shift_resources (
  id int8 not null,
  kind varchar(255),
  name varchar(255),
  open_shift_resource_id varchar(255),
  resource_details varchar(65535),
  primary key (id)
);

alter table application_prod_request_open_shift_resources
  add constraint UK_4fs0kt4rxy4ptky8072yyfax6 unique (open_shift_resources_id);

alter table application_prod_request
  add constraint FK5mw44b0j9a8o7hdxmqgqh3l49
foreign key (actioned_by_id)
references team_member;

alter table application_prod_request
  add constraint FKe95ht85ja7k87klfthst1aps2
foreign key (application_id)
references application;

alter table application_prod_request_open_shift_resources
  add constraint FKi2kv6of4rt2a0cm5dr23rgvb6
foreign key (open_shift_resources_id)
references open_shift_resources;

alter table application_prod_request_open_shift_resources
  add constraint FKgogoefokghir3c2i104u618vs
foreign key (application_prod_request_entity_id)
references application_prod_request;