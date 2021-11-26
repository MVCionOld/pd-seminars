ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;

SELECT TRANSFORM(ip)
USING 'cut -d . -f 1' AS ip
FROM Subnets
LIMIT 10;

ADD FILE ./parse.sh;
SELECT TRANSFORM(ip)
USING './parse.sh' AS ip2
FROM Subnets
LIMIT 10;
