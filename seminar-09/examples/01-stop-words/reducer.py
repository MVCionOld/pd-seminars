#!/usr/bin/env python

import sys

current_key = None
sum_count = 0
for line in sys.stdin:
    try:
        key, count = line.strip().split('\t', 1)
        count = int(count)
    except ValueError as e:
        continue
    if current_key != key:
        if current_key:
            print "%s\t%d" % (current_key, sum_count)
        sum_count = 0
        current_key = key
    sum_count += count

if current_key:
    print "%s\t%d" % (current_key, sum_count)
