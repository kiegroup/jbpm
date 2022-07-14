ALTER TABLE TaskEvent ADD COLUMN currentOwner varchar(255);
alter table NodeInstanceLog add column observation varchar(255);

create table TimerMappingInfo (
	id int8 not null, 
	externalTimerId varchar(255), 
	kieSessionId int8 not null, 
	timerId int8 not null, 
	uuid varchar(255) not null, 
	info oid, 
	primary key (id)
);

create sequence TIMER_MAPPING_INFO_ID_SEQ;

create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);
