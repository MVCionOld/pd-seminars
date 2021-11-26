ADD JAR FirstOctet/target/FirstOctet-1.0-SNAPSHOT.jar;

USE mtsion_test;

CREATE TEMPORARY FUNCTION FirstOctet AS 'com.pd.FirstOctetUDF';

SELECT
    FirstOctet(ip)
FROM
    Subnets
LIMIT
	10;
