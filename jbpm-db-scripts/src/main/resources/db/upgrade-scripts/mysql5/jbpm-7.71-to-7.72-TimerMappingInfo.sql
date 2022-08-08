ALTER TABLE TimerMappingInfo ADD COLUMN processInstanceId bigint;
ALTER TABLE TimerMappingInfo MODIFY timerId bigint NULL;