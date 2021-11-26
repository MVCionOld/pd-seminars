ADD JAR Identity/target/Identity-1.0-SNAPSHOT.jar;

USE mtsion_test;

CREATE TEMPORARY FUNCTION IDENTITY AS 'com.hobod.IdentityUDF';

SELECT
    identity(ip)
FROM
    Subnets
LIMIT
    10;
