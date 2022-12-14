from __future__ import print_function
from datetime import datetime as dt

import re
from pyspark.sql import SparkSession
from pyspark.sql.functions import col
from pyspark.sql.types import *


spark = SparkSession.builder\
    .appName('Spark DF practice')\
    .master('yarn')\
    .getOrCreate()

# 3.4 G    10.2 G   /data/access_logs/big_log
# 17.6 M   52.7 M   /data/access_logs/big_log_10000
# 175.4 M  526.2 M  /data/access_logs/big_log_100000
dataset_path = "/data/access_logs/big_log_10000"

log_format = re.compile(
    r"(?P<host>[\d\.]+)\s"
    r"(?P<identity>\S*)\s"
    r"(?P<user>\S*)\s"
    r"\[(?P<time>.*?)\]\s"
    r'"(?P<request>.*?)"\s'
    r"(?P<status>\d+)\s"
    r"(?P<bytes>\S*)\s"
    r'"(?P<referer>.*?)"\s'
    r'"(?P<user_agent>.*?)"\s*'
)


def parse_line(line):
    match = log_format.match(line)
    if not match:
        return "", "", "", "", "", "", "" , "", ""

    request = match.group('request').split()
    return (
        match.group('host'),
        match.group('time').split()[0],
        request[0],
        request[1],
        int(match.group('status')),
        int(match.group('bytes')),
        match.group('referer'),
        match.group('user_agent'),
        dt.strptime(match.group('time').split()[0], '%d/%b/%Y:%H:%M:%S').hour
    )


schema = StructType(fields=[
    StructField("ip", StringType()),
    StructField("dttm", StringType()),
    StructField("method", StringType()),
    StructField("path", StringType()),
    StructField("status", IntegerType()),
    StructField("bytes", IntegerType()),
    StructField("referer", StringType()),
    StructField("user_agent", StringType()),
    StructField("hour", IntegerType())
])


lines_rdd = spark.sparkContext.textFile(dataset_path)
parsed_logs_rdd = lines_rdd.map(parse_line)

logs_df = spark.createDataFrame(
    parsed_logs_rdd,
    schema=schema,
    verifySchema=False
).cache()

# RDD-like queries:

rdd = logs_df.rdd.filter(lambda row: "4" in row[0])\
    .map(lambda row: (row[0], 1))\
    .reduceByKey(lambda a, b: a + b)\
    .sortBy(lambda a: -a[1])
for (uni, cnt) in rdd.take(10):
    print(uni.encode("utf-8"), cnt, sep="\t")

# DataFrame-like queries:

logs_df[logs_df.ip.like("%4%")]\
    .groupby('ip')\
    .count()\
    .orderBy(col("count").desc())\
    .show(10)

# SQL-like queries:

sql_string = """
    SELECT 
        ip, count(*) AS cnt
    FROM 
        log_df
    WHERE 
        ip LIKE "%4%" 
    GROUP BY
        ip
    ORDER BY
        cnt DESC
"""
logs_df.registerTempTable("log_df")
spark.sql(sql_string).show(10)
