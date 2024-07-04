ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId bigint;
ALTER TABLE TimerMappingInfo MODIFY timerId bigint NULL;
DROP INDEX IDX_EventTypes_element;
DROP INDEX IDX_EventTypes_compound;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);