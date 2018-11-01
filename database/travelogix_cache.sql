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
	StartDate		DATE			not null,
	EndDate			DATE			not null,
	constraint CacheJobsAcco_PK primary key ("SUPPID", "CREDNAME", "CITYCODE", "JOBSEQ")
);

create unique index CacheJobsAcco_JobSeq ON CacheJobsAcco ("JOBSEQ");

--**************************************************************************************************
-- Create tables/indexes for Acco cache job definitions and individual jobs.
--**************************************************************************************************

drop table CacheJobsDefnAir cascade constraints purge;

create table CacheJobsDefnAir (
	SuppID				VARCHAR2(20) 	not null, 
	CredName			VARCHAR2(50) 	not null, 
	Origin				VARCHAR2(20) 	not null, 
	Destination			VARCHAR2(20) 	not null, 
	TripType			VARCHAR2(10) 	not null check (TripType in ('OneWay', 'Return')),
	CabinClass			VARCHAR2(10)	not null check (CabinClass in ('Business', 'Economy', 'First')),
	BookDaysRange		INTEGER		not null,
	TripDurRange		INTEGER		not null,
	AdultPsgrsRange		INTEGER		not null,
	ChildPsgrsRange		INTEGER		not null,
	InfantPsgrsRange	INTEGER		not null,
	constraint CacheJobsDefnAir_PK primary key ("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION", "TRIPTYPE")
);

drop table CacheJobsAir cascade constraints purge;

create table CacheJobsAir (
	SuppID			VARCHAR2(20) 	not null, 
	CredName		VARCHAR2(50) 	not null, 
	Origin			VARCHAR2(20) 	not null, 
	Destination		VARCHAR2(20) 	not null, 
	TripType		VARCHAR2(10) 	not null,
	CabinClass		VARCHAR2(10)	not null,
	JobSeq			INTEGER			not null,
	StartDate		DATE			not null,
	ReturnDate		DATE					 ,
	AdultPsgrs		INTEGER		not null,
	ChildPsgrs		INTEGER		not null,
	InfantPsgrs		INTEGER		not null,
	constraint CacheJobsAir_PK primary key("SUPPID", "CREDNAME", "ORIGIN", "DESTINATION", "TRIPTYPE", "JOBSEQ")
);

create unique index CacheJobsAir_JobSeq ON CacheJobsAir ("JOBSEQ");


--**************************************************************************************************
-- Stored procedure to generate Air jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsAir is
begin
	
	delete from CacheJobsAir;

	for jobAir in (select SuppID, CredName, Origin, Destination, TripType, CabinClass, BookDaysRange, TripDurRange, AdultPsgrsRange, ChildPsgrsRange, InfantPsgrsRange, (rownum * 1000000) JobDefSeq from CacheJobsDefnAir order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobAir.JobDefSeq + (rownum * 10000)) JobDefSeq, StrDt, decode(upper(jobAir.TripType), 'RETURN', (StrDt + durDays), NULL) RetDt, AdultPassNo from
								( select (sysdate + rownum) StrDt from dual connect by level <= jobAir.BookDaysRange ),
								( select rownum AdultPassNo from dual connect by level <= jobAir.AdultPsgrsRange ),
								( select rownum durDays from dual connect by level < = decode(upper(jobAir.TripType), 'RETURN', jobAir.TripDurRange, 1) ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildPassNo from dual connect by level <= (jobAir.ChildPsgrsRange + 1))
			loop

				for dataRg3 in (select (dataRg2.JobDefSeq + rownum) JobDefSeq, (level - 1) InfantPassNo from dual connect by level <= (jobAir.InfantPsgrsRange + 1))
				loop
					insert into CacheJobsAir (SuppID, CredName, Origin, Destination, TripType, CabinClass, JobSeq, StartDate, ReturnDate, AdultPsgrs, ChildPsgrs, InfantPsgrs)
						values (jobAir.SuppID, jobAir.CredName, jobAir.Origin, jobAir.Destination, jobAir.TripType, jobAir.CabinClass, dataRg3.JobDefSeq, dataRg1.StrDt, dataRg1.RetDt, dataRg1.AdultPassNo, dataRg2.ChildPassNo, dataRg3.InfantPassNo);
				end loop;
				
			end loop;

		end loop;
	end loop;
end;
/

--**************************************************************************************************
-- Stored procedure to generate Accommodation jobs from job definitions.
--**************************************************************************************************
create or replace procedure GenerateCacheJobsAcco is
begin
	
	delete from CacheJobsAcco;

	for jobAcco in (select SuppID, CredName, CountryCode, CityCode, BookDaysRange, RoomsRange, AdultGuestsRange, ChildGuestsRange, TripDurRange, (rownum * 1000000000) JobDefSeq from CacheJobsDefnAcco order by SuppID, CredName)
	loop
	
		for dataRg1 in (select (jobAcco.JobDefSeq + (rownum * 10000)) JobDefSeq, StrDt, (StrDt + DurDays) EndDt, AdultGuestsNo from 
								( select (sysdate + rownum) StrDt from dual connect by level <= jobAcco.BookDaysRange ),
								( select rownum AdultGuestsNo from dual connect by level <= jobAcco.AdultGuestsRange ),
								( select rownum DurDays from dual connect by level < = jobAcco.TripDurRange ) )
		loop

			for dataRg2 in (select (dataRg1.JobDefSeq + (rownum * 100)) JobDefSeq, (level - 1) ChildGuestsNo from dual connect by level <= (jobAcco.ChildGuestsRange + 1))
			loop
				insert into CacheJobsAcco (SuppID, CredName, CountryCode, CityCode, JobSeq, Rooms, AdultGuests, ChildGuests, StartDate, EndDate)
				(
					select jobAcco.SuppID, jobAcco.CredName, jobAcco.CountryCode, jobAcco.CityCode, (dataRg2.JobDefSeq + rownum), noOfRooms, dataRg1.AdultGuestsNo, dataRg2.ChildGuestsNo, dataRg1.StrDt, dataRg1.EndDt
							from ( select rownum noOfRooms from dual connect by level <= decode(sign(dataRg1.AdultGuestsNo - jobAcco.RoomsRange), -1, dataRg1.AdultGuestsNo, jobAcco.RoomsRange) )
				);
			end loop;

		end loop;
	end loop;
end;
/