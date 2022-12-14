from __future__ import print_function

import re
from pyspark import SparkConf
from pyspark import SparkContext

conf = SparkConf().setMaster("yarn") \
    .setAppName("Griboedov WordCount with accumulator")
sc = SparkContext(conf=conf)

total_acc = sc.accumulator(0)

rdd = sc.textFile("/data/griboedov")
rdd = rdd.map(lambda x: x.strip())
rdd = rdd.flatMap(lambda x: re.split("\s+", x))
rdd = rdd.map(lambda x: re.sub(u"\\W+", "", x.strip(), flags=re.U))
rdd = rdd.filter(lambda x: len(x) >= 3 and x.istitle())
rdd = rdd.map(lambda x: (x, 1))
rdd = rdd.reduceByKey(lambda a, b: a + b)
# caching to prevent the second
# calculation of rdd for '.foreach<>'
rdd = rdd.sortBy(lambda a: -a[1]).cache()

# firstly, display top-10
for (uni, cnt) in rdd.take(10):
    print(uni.encode("utf-8"), cnt, sep=" => ")

# then calculate overall total
rdd.foreach(lambda row: total_acc.add(row[1]))
print("Total: ", total_acc.value)  # Total:  4579
