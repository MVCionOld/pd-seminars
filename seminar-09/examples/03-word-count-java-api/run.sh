#! /usr/bin/env bash

mvn package
hdfs dfs -rm -r -skipTrash wordcount_out 2> /dev/null
# hadoop jar jar/WordCount.jar ru.mipt.examples.WordCount /data/griboedov wordcount_out
yarn jar target/WordCount-1.0.jar ru.mipt.examples.WordCount /data/griboedov wordcount_out
