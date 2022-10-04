ALTER TABLE TimerMappingInfo ADD processInstanceId number(19,0);
ALTER TABLE TimerMappingInfo MODIFY (timerId NULL);
DROP INDEX IDX_EventTypes_element;
CREATE INDEX IDX_EventTypes_IdElement ON EventTypes(InstanceId,element);