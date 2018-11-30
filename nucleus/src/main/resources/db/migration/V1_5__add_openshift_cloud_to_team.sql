alter table team add column open_shift_cloud varchar(255);
update team set open_shift_cloud = 'ab-cloud';