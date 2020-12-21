alter table CorrelationKeyInfo modify name varchar(255) not null;
alter table CorrelationKeyInfo add constraint IDX_CorrelationKeyInfo_name unique (name);