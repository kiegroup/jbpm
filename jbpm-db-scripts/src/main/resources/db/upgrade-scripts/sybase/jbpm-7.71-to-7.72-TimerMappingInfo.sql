ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId numeric(19,0);
go
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId numeric(19,0);
go