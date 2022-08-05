ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId bigint;
ALTER TABLE TimerMappingInfo ALTER COLUMN timerId bigint;
