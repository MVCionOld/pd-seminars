from __future__ import print_function
from datetime import datetime as dt
from operator import add

import re
from pyspark import SparkConf
from pyspark import SparkContext

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

conf = SparkConf().setMaster("yarn") \
    .setAppName("Parsed logs with top-5")
sc = SparkContext(conf=conf)


def parse_line(line):
    match = log_format.match(line)
    if not match:
        return "", "", "", "", "", "", "" , "", ""

    request = match.group('request').split()
    return (match.group('host'), match.group('time').split()[0],
            request[0], request[1], match.group('status'), match.group('bytes'),
            match.group('referer'), match.group('user_agent'),
            dt.strptime(match.group('time').split()[0], '%d/%b/%Y:%H:%M:%S').hour)


lines = sc.textFile(dataset_path)
parsed_logs = lines.map(parse_line).cache()

rdd = parsed_logs.filter(lambda row: "4" in row[0])
rdd = rdd.map(lambda row: (row[0], 1))
rdd = rdd.reduceByKey(add)  # equal: `rdd = rdd.reduceByKey(lambda a, b: a + b)`
rdd = rdd.sortBy(lambda a: -a[1])

for (uni, cnt) in rdd.take(10):
    print(uni.encode("utf-8"), cnt, sep="\t")
