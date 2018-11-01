--**************************************************************************************************
-- Create tables/indexes for Acco cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnAcco cascade constraints purge;

create table CacheJobsDefnAcco (
	SuppID				VARCHAR2(20)	not null, 
	CredName			VARCHAR2(50)	not null, 
	CountryCode			VARCHAR2(50)	not null,
	CityCode			VARCHAR2(20)	not null, 
	BookDaysRange		INTEGER		not null,	
	RoomsRange			INTEGER		not null, 
	AdultGuestsRange	INTEGER		not null,
	ChildGuestsRange	INTEGER		not null,
	InfantGuestsRange	INTEGER		not null,	
	TripDurRange		INTEGER		not null,
	constraint CacheJobsDefnAcco_PK primary key ("SUPPID", "CREDNAME", "CITYCODE")
);

drop table CacheJobsAcco cascade constraints purge;

create table CacheJobsAcco (
	SuppID			VARCHAR2(20)	not null, 
	CredName		VARCHAR2(50)	not null, 
	CountryCode		VARCHAR2(20)	not null,
	CityCode		VARCHAR2(20)	not null,
	JobSeq			INTEGER			not null,
	Rooms			INTEGER		not null, 
	AdultGuests		INTEGER		not null,
	ChildGuests		INTEGER		not null,
	InfantGuests	INTEGER		not null,
	StartDate		DATE			not null,
	EndDate			DATE			not null,
	constraint CacheJobsAcco_PK primary key ("SUPPID", "CREDNAME", "CITYCODE", "JOBSEQ")
);

create unique index CacheJobsAcco_JobSeq ON CacheJobsAcco ("JOBSEQ");

--**************************************************************************************************
-- Stored procedure to generate Accommodation jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsAcco is
begin
	
	delete from CacheJobsAcco;

	for jobAcco in (select SuppID, CredName, CountryCode, CityCode, BookDaysRange, RoomsRange, AdultGuestsRange, ChildGuestsRange, InfantGuestsRange, TripDurRange, (rownum * 1000000000) JobDefSeq from CacheJobsDefnAcco order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobAcco.JobDefSeq + (rownum * 10000)) JobDefSeq, StrDt, (StrDt + DurDays) EndDt, AdultGuestsNo from 
								( select (sysdate + rownum) StrDt from dual connect by level <= jobAcco.BookDaysRange ),
								( select rownum AdultGuestsNo from dual connect by level <= jobAcco.AdultGuestsRange ),
								( select rownum DurDays from dual connect by level <= jobAcco.TripDurRange ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildGuestsNo from dual connect by level <= (jobAcco.ChildGuestsRange + 1))
			loop

				for dataRg3 in (select (dataRg2.JobDefSeq + rownum) JobDefSeq, (level - 1) InfantGuestsNo from dual connect by level <= (jobAcco.InfantGuestsRange + 1))
				loop
					insert into CacheJobsAcco (SuppID, CredName, CountryCode, CityCode, JobSeq, Rooms, AdultGuests, ChildGuests, InfantGuests, StartDate, EndDate)
					(
						select jobAcco.SuppID, jobAcco.CredName, jobAcco.CountryCode, jobAcco.CityCode, (dataRg3.JobDefSeq + rownum), noOfRooms, dataRg1.AdultGuestsNo, dataRg2.ChildGuestsNo, dataRg3.InfantGuestsNo, dataRg1.StrDt, dataRg1.EndDt
								from ( select rownum noOfRooms from dual connect by level <= decode(sign(dataRg1.AdultGuestsNo - jobAcco.RoomsRange), -1, dataRg1.AdultGuestsNo, jobAcco.RoomsRange) )
					);
				end loop;
			end loop;

		end loop;
	end loop;
end;
/