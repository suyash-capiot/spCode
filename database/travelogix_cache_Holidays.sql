--**************************************************************************************************
-- Create tables/indexes for Holidays cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnHolidays cascade constraints purge;

create table CacheJobsDefnHolidays (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null,
	TourID				VARCHAR2(20)	not null,
	TourSubID			VARCHAR2(20),
	Brand				VARCHAR2(20),
	constraint CacheJobsDefnHolidays_PK primary key ("SUPPID", "CREDNAME", "TOURID", "TOURSUBID", "BRAND")
);

drop table CacheJobsHolidays cascade constraints purge;

create table CacheJobsHolidays (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	TourID				VARCHAR2(20)	not null,
	TourSubID			VARCHAR2(20),
	Brand				VARCHAR2(20),
	JobSeq				INTEGER			not null,
	constraint CacheJobsHolidays_PK primary key("SUPPID", "CREDNAME", "TOURID", "TOURSUBID", "BRAND", "JOBSEQ")
);

create unique index CacheJobsHolidays_JobSeq ON CacheJobsHolidays ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Holidays jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsHolidays is
begin
	
	delete from CacheJobsHolidays;

	for jobHolidays in (select SuppID, CredName, TourID, TourSubID, Brand, (rownum * 1000000) JobDefSeq from CacheJobsDefnHolidays order by SuppID, CredName)
	loop

		insert into CacheJobsHolidays (SuppID, CredName, TourID, TourSubID, Brand, JobSeq)
			values (jobHolidays.SuppID, jobHolidays.CredName, jobHolidays.TourID, jobHolidays.TourSubID, jobHolidays.Brand, jobHolidays.JobDefSeq);

	end loop;
end;
/
