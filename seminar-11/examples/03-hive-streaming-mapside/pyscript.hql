ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;

ADD FILE ./parse.py;
SELECT TRANSFORM(ip)
USING './parse.py' AS ip2
FROM Subnets
LIMIT 10;
