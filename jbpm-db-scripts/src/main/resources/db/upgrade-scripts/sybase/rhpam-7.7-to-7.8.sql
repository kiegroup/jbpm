create table PlanningTask (
    taskId numeric(19,0) not null,
    OPTLOCK int null,
    assignedUser varchar(255) null,
    taskIndex int not null,
    lastModificationDate datetime null,
    published smallint not null,
    primary key (taskId)
) lock datarows
go

CREATE INDEX IDX_PAsPot_TaskEntity ON PeopleAssignments_PotOwners(task_id,entity_id)
go
create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser)
go