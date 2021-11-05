#! /usr/bin/env bash

reducer_num=8

for result_file in `hadoop fs -ls wordcount_out | awk '{print $8}' | tail -$reducer_num`
do
    hadoop fs -cat $result_file | head
done
