#! /usr/bin/env bash

rm -r wordcount_out
hadoop --config /opt/cloudera/parcels/CDH/etc/hadoop/conf.empty jar jar/WordCount.jar ru.mipt.examples.WordCount /home/velkerr/griboedov_sample wordcount_out
