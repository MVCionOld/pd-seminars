#!/usr/bin/env bash

OUT_DIR='Monte-Carlo-PI'
NUM_REDUCERS=0

hdfs dfs -rm -r -skipTrash ${OUT_DIR}

gcc -std=c99 -static -flto -o mapper.out mapper.c

yarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \
    -D mapreduce.job.name="MR Streaming job on C lang" \
    -D mapreduce.job.reduces=${NUM_REDUCERS} \
    -files ./mapper.out \
    -mapper "./mapper.out" \
    -input /datasets/stop_words_en.txt \
    -output ${OUT_DIR}
