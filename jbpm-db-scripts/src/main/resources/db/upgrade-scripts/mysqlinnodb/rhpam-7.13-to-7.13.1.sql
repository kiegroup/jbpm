ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId bigint;
ALTER TABLE TimerMappingInfo MODIFY timerId bigint NULL;
DROP INDEX IDX_EventTypes_element on EventTypes;
DROP INDEX IDX_EventTypes_compound on EventTypes;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);