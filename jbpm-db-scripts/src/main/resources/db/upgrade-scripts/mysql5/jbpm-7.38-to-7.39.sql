create table PlanningTask (
    taskId bigint not null,
    OPTLOCK integer,
    assignedUser varchar(255),
    taskIndex integer not null,
    lastModificationDate datetime,
    published smallint not null,
    primary key (taskId)
);

CREATE INDEX IDX_PAsPot_TaskEntity ON PeopleAssignments_PotOwners(task_id,entity_id);
create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);