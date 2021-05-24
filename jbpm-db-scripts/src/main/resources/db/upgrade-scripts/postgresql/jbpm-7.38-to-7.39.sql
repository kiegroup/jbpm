create table PlanningTask (
    taskId int8 not null,
    OPTLOCK int4,
    assignedUser varchar(255),
    taskIndex int4 not null,
    lastModificationDate timestamp,
    published int2 not null,
    primary key (taskId)
);

CREATE INDEX IDX_PAsPot_TaskEntity ON PeopleAssignments_PotOwners(task_id,entity_id);
create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);