#!/usr/bin/env python

import sys
import re

reload(sys)
sys.setdefaultencoding('utf-8') # required to convert to unicode

for line in sys.stdin:
    try:
        article_id, text = unicode(line.strip()).split('\t', 1)
    except ValueError as e:
        continue
    words = re.split('\W*\s+\W*', text, flags=re.UNICODE)
    for word in words:
        print "%s\t%d" % (word.lower(), 1)
