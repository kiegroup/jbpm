    create table PlanningTask (
        taskId bigint not null,
        OPTLOCK int,
        assignedUser varchar(255),
        taskIndex int not null,
        lastModificationDate datetime2,
        published smallint not null,
        primary key (taskId)
    );

    alter table PlanningTask
        add constraint FK_PlanningTask_Task
        foreign key (taskId)
        references Task;

    create index IDX_PlanningTask_assignedUser on PlanningTask(assignedUser);