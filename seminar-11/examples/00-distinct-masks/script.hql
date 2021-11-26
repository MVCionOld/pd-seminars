ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;

SELECT COUNT(DISTINCT mask)
FROM Subnets;