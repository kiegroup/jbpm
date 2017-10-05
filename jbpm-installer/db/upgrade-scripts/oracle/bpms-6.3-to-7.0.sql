alter table RequestInfo add priority number(10,0);
update RequestInfo set priority = 5;