ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId int8;
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId int8;