ALTER TABLE TaskEvent ADD COLUMN currentOwner varchar(255);
alter table NodeInstanceLog add column observation varchar(255);

create table TimerMappingInfo (
	id bigint not null auto_increment, 
	externalTimerId varchar(255), 
	kieSessionId bigint not null, 
	timerId bigint not null, 
	uuid varchar(255) not null, 
	info longblob, 
	primary key (id)
) ENGINE=InnoDB;

create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);
