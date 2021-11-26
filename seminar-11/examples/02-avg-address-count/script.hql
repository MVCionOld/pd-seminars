ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;
USE mapred.job.name=count-avg-ips;

with IpsByMasks as (
    select count(distinct ip) cnt
    from Subnets
    group by mask
)
select avg(cnt)
from IpsByMasks;

select avg(IpsByMasks.cnt)
from (
    select count(distinct ip) cnt
    from Subnets
    group by mask
) as IpsByMasks;

