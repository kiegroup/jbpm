    create table PlanningTask (
        taskId numeric(19,0) not null,
        OPTLOCK int,
        assignedUser varchar(255),
        taskIndex int not null,
        lastModificationDate datetime,
        published smallint not null,
        primary key (taskId)
    );

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);