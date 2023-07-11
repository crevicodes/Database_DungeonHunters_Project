
select * from Hunter H
where H.guildid = (select G.guildid from Guild G
                   where G.name = x);
                   
select * from Guild G
where G.regioncode = (select R.regioncode from Region R
                      where R.regionname = x);
                      

select H.guildid, count(*) from Hunter H
where H.guildid is not null
group by H.guildid;


select G.regioncode, count(*) from Guild G
group by G.regioncode;



alter table Dungeon add constraint dungeon_monster_count_ck check MaxMonsterCount > MinMonsterCount;

alter table Hunter_Association_Employee add constraint emp_emptype_data_ck check EmpType in (1,2);

alter table Receptionist add constraint recep_auth_data_ck check AuthorizationLevel in (1,2); 