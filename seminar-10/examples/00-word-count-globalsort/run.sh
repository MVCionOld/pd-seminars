#!/usr/bin/env bash

IN_DIR="/data/minecraft_user_activity"
OUT_DIR="minecraft_result"

# Look at the input data
hdfs dfs -cat ${IN_DIR}/part-00000 | head -n 10

# Remove previous results
hdfs dfs -rm -r -skipTrash ${OUT_DIR}* > /dev/null

yarn jar /opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/hadoop-streaming.jar \
    -D stream.num.map.output.key.fields=3 \
    -D mapreduce.job.reduces=1 \
    -D mapreduce.job.output.key.comparator.class=org.apache.hadoop.mapreduce.lib.partition.KeyFieldBasedComparator \
    -D mapreduce.partition.keycomparator.options='-k2nr -k1' \
    -mapper cat \
    -reducer cat \
    -input ${IN_DIR} \
    -output ${OUT_DIR}

# Checking result
hdfs dfs -cat ${OUT_DIR}/part-00000 | head -n 10