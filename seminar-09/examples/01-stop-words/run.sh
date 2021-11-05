#!/usr/bin/env bash

OUT_DIR="streaming_wc_result"
NUM_REDUCERS=8

hadoop fs -rm -r -skipTrash $OUT_DIR*

yarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \
    -D mapred.job.name="StopWords example" \
    -D mapreduce.job.reduces=${NUM_REDUCERS} \
    -files mapper.py,reducer.py,/datasets/stop_words_en.txt \
    -mapper mapper.py \
    -reducer reducer.py \
    -input /data/wiki/en_articles_part \
    -output $OUT_DIR

for num in `seq 0 $(($NUM_REDUCERS - 1))`
do
    hdfs dfs -cat ${OUT_DIR}/part-0000$num | head
done
