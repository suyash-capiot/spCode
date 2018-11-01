--**************************************************************************************************
-- Create tables/indexes for SightSeeing (Activities) cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnSight cascade constraints purge;

create table CacheJobsDefnSight (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	CountryCode			VARCHAR2(20) 	not null, 
	CityCode			VARCHAR2(20) 	not null, 
	SearchDaysRange		INTEGER,
	SearchDurRange		INTEGER,
	AdultCountRange		INTEGER		not null,
	ChildCountRange		INTEGER		not null,
	constraint CacheJobsDefnSight_PK primary key ("SUPPID", "CREDNAME", "COUNTRYCODE", "CITYCODE")
);

drop table CacheJobsSight cascade constraints purge;

create table CacheJobsSight (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	CountryCode		VARCHAR2(20) 	not null, 
	CityCode		VARCHAR2(20) 	not null, 
	JobSeq			INTEGER			not null,
	StartDate		DATE,
	EndDate			DATE					 ,
	AdultCount		INTEGER		not null,
	ChildCount		INTEGER		not null,
	constraint CacheJobsSight_PK primary key("SUPPID", "CREDNAME", "COUNTRYCODE", "CITYCODE", "JOBSEQ")
);

create unique index CacheJobsSight_JobSeq ON CacheJobsSight ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate SightSeeing (Activities) jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsSight is
begin
	
	delete from CacheJobsSight;

	for jobSight in (select SuppID, CredName, CountryCode, CityCode, SearchDaysRange, SearchDurRange, AdultCountRange, ChildCountRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnSight order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobSight.JobDefSeq + (rownum * 10000)) JobDefSeq, StrDt, decode(jobSight.SearchDaysRange, NULL, NULL, (StrDt + durDays)) EndDt, AdultPassNo from
								( select decode(jobSight.SearchDaysRange, NULL, NULL, (sysdate + rownum)) StrDt from dual connect by level <= decode(jobSight.SearchDaysRange, NULL, NULL, 1) ),
								( select rownum AdultPassNo from dual connect by level <= jobSight.AdultCountRange ),
								( select rownum durDays from dual connect by level < = decode(jobSight.SearchDaysRange, NULL, NULL, jobSight.SearchDurRange) ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildPassNo from dual connect by level <= (jobSight.ChildCountRange + 1))
			loop

				insert into CacheJobsSight (SuppID, CredName, CountryCode, CityCode, JobSeq, StartDate, EndDate, AdultCount, ChildCount)
					values (jobSight.SuppID, jobSight.CredName, jobSight.CountryCode, jobSight.CityCode, dataRg2.JobDefSeq, dataRg1.StrDt, dataRg1.EndDt, dataRg1.AdultPassNo, dataRg2.ChildPassNo);
			
			end loop;

		end loop;
	end loop;
end;
/
