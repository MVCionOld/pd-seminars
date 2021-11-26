ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;

select count(distinct ip)
from Subnets
where mask = '255.255.255.128';

select count(distinct ip)
from SubnetsPart
where mask = '255.255.255.128';
