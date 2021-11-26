ADD JAR OctetsSplit/target/OctetsSplit-1.0-SNAPSHOT.jar;

USE mtsion_test;

CREATE TEMPORARY FUNCTION OctetsSplit as 'com.pd.OctetsSplitUDTF';

WITH Ips AS (
	SELECT DISTINCT
		ip
	FROM
		Subnets
)
SELECT
	OctetsSplit(ip)
FROM
	Ips
ORDER BY
	ip, octet_num
LIMIT
	40;
