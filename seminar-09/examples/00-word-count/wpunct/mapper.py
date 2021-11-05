#!/usr/bin/env python

import sys

for line in sys.stdin:
    try:
        article_id, text = line.strip().split('\t', 1)
    except ValueError as e:
        continue
    words = text.split(' ')
    for word in words:
        print "%s\t%d" % (word, 1)
