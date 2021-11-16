
create table TimerMappingInfo (
	id number(19,0) not null, 
	externalTimerId varchar2(255 char), 
	kieSessionId number(19,0) not null, 
	timerId number(19,0) not null, 
	uuid varchar2(255 char) not null, 
	primary key (id)
);

create sequence TIMER_MAPPING_INFO_ID_SEQ;

create unique index IDX_TMI_KSessionUUID on TimerMappingInfo (kieSessionId, uuid);