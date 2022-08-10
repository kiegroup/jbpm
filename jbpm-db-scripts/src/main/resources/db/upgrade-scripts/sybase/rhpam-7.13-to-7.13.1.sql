ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId numeric(19,0);
go
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId numeric(19,0);
go
DROP INDEX EventTypes.IDX_EventTypes_element;
go
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);
go
