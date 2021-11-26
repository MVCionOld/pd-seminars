ADD JAR /opt/cloudera/parcels/CDH/lib/hive/lib/hive-contrib.jar;

USE mtsion_test;

SET mapred.job.name="non-partitioned-count-distinct";

SELECT COUNT(DISTINCT mask)
FROM SubnetsPart;