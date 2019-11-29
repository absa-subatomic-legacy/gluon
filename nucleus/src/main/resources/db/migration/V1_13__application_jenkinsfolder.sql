alter table application add column jenkins_folder varchar(255);
update application set jenkins_folder = '.';