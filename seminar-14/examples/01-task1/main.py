from __future__ import print_function

import re
from pyspark import SparkConf
from pyspark import SparkContext

conf = SparkConf().setMaster("yarn")\
    .setAppName("Griboedov WordCount with filtering")
sc = SparkContext(conf=conf)

rdd = sc.textFile("/data/griboedov")
rdd = rdd.map(lambda x: x.strip().lower())
rdd = rdd.flatMap(lambda x: re.split("\s+", x))
rdd = rdd.map(lambda x: re.sub(u"\\W+", "", x.strip(), flags=re.U))
rdd = rdd.filter(lambda x: len(x) >= 3)
rdd = rdd.map(lambda x: (x, 1))
rdd = rdd.reduceByKey(lambda a, b: a + b)
rdd = rdd.sortBy(lambda a: -a[1])

for (uni, cnt) in rdd.take(10):
    print(uni.encode("utf-8"), cnt, sep=" => ")
