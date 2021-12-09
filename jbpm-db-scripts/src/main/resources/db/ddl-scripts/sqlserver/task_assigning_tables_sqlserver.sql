    create table PlanningTask (
        taskId bigint not null,
        OPTLOCK int,
        assignedUser varchar(255),
        taskIndex int not null,
        lastModificationDate datetime2,
        published smallint not null,
        primary key (taskId)
    );

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);