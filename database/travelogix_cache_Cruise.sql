--**************************************************************************************************
-- Create tables/indexes for Cruise cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnCruise cascade constraints purge;

create table CacheJobsDefnCruise (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null,
	Port				VARCHAR2(20)	not null,
	SailDaysRange		INTEGER		not null,
	SearchDurRange		INTEGER		not null,
	AdultPsgrsRange		INTEGER		not null,
	ChildPsgrsRange		INTEGER		not null,
	InfantPsgrsRange	INTEGER		not null,
	constraint CacheJobsDefnCruise_PK primary key ("SUPPID", "CREDNAME", "PORT")
);

drop table CacheJobsCruise cascade constraints purge;

create table CacheJobsCruise (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	Port			VARCHAR2(20) 	not null, 
	JobSeq			INTEGER			not null,	
	SailStartDate	DATE			not null,
	SailEndDate		DATE			not null,
	AdultPsgrs		INTEGER		not null,
	ChildPsgrs		INTEGER		not null,
	InfantPsgrs		INTEGER		not null,
	constraint CacheJobsCruise_PK primary key("SUPPID", "CREDNAME", "PORT", "JOBSEQ")
);

create unique index CacheJobsCruise_JobSeq ON CacheJobsCruise ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Cruise jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsCruise is
begin
	
	delete from CacheJobsCruise;

	for jobCruise in (select SuppID, CredName, Port, SailDaysRange, SearchDurRange, AdultPsgrsRange, ChildPsgrsRange, InfantPsgrsRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnCruise order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobCruise.JobDefSeq + (rownum * 10000)) JobDefSeq, SailStrDt, (SailStrDt + durDays) SailEndDt, AdultPassNo from
								( select (sysdate + rownum) SailStrDt from dual connect by level <= jobCruise.SailDaysRange ),
								( select rownum AdultPassNo from dual connect by level <= jobCruise.AdultPsgrsRange ),
								( select rownum durDays from dual connect by level < = jobCruise.SearchDurRange ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildPassNo from dual connect by level <= (jobCruise.ChildPsgrsRange + 1))
			loop

				for dataRg3 in (select (dataRg2.JobDefSeq + rownum) JobDefSeq, (level - 1) InfantPassNo from dual connect by level <= (jobCruise.InfantPsgrsRange + 1))
				loop
					insert into CacheJobsCruise (SuppID, CredName, Port, JobSeq, SailStartDate, SailEndDate, AdultPsgrs, ChildPsgrs, InfantPsgrs)
						values (jobCruise.SuppID, jobCruise.CredName, jobCruise.Port, dataRg3.JobDefSeq, dataRg1.SailStrDt, dataRg1.SailEndDt, dataRg1.AdultPassNo, dataRg2.ChildPassNo, dataRg3.InfantPassNo);
				end loop;
				
			end loop;

		end loop;
	end loop;
end;
/
