alter table PlanningTask drop constraint FK_PlanningTask_Task;
drop table if exists PlanningTask cascade;