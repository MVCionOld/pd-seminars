#!/usr/bin/env bash

OUT_DIR="_mr_task1"$(date +"%s%6N")
NUM_REDUCERS=4

hdfs dfs -rm -r -skipTrash ${OUT_DIR}

yarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \
    -D mapreduce.job.name="Assignment MR task1 step1" \
    -D mapreduce.job.reduces=${NUM_REDUCERS} \
    -files mapper.py,reducer.py,/datasets/stop_words_en.txt \
    -mapper "python ./mapper.py" \
    -reducer "python ./reducer.py" \
    -input /data/wiki/en_articles_part \
    -output ${OUT_DIR}
