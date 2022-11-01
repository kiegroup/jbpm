ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId int8;
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId DROP NOT NULL;
DROP INDEX IDX_EventTypes_element;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);