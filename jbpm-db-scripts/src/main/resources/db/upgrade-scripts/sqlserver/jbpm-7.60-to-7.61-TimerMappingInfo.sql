
create table TimerMappingInfo (
	id bigint numeric(19,0) identity not null, 
	externalTimerId varchar(255), 
	kieSessionId numeric(19,0) not null, 
	timerId numeric(19,0) not null, 
	uuid varchar(255) not null, 
	primary key (id)
);

create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);