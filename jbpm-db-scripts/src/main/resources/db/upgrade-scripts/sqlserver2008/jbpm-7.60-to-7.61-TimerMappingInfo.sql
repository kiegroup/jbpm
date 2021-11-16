
create table TimerMappingInfo (
	id bigint identity not null, 
	externalTimerId varchar(255), 
	kieSessionId bigint not null, 
	timerId bigint not null, 
	uuid varchar(255) not null, 
	primary key (id)
);

create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);