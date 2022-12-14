from pyspark import SparkConf
from pyspark import SparkContext

conf = SparkConf().setMaster("yarn").setAppName("Griboedov WordCount")
sc = SparkContext(conf=conf)

rdd = sc.textFile("/data/griboedov")
rdd = rdd.map(lambda x: x.strip().lower())
rdd = rdd.flatMap(lambda x: x.split(" "))
rdd = rdd.map(lambda x: (x, 1))
rdd = rdd.reduceByKey(lambda a, b: a + b)
rdd = rdd.sortBy(lambda a: -a[1])

print(rdd.take(10))
