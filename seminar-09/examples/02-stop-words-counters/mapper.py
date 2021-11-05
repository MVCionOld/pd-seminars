import sys
import re
from collections import defaultdict

reload(sys)
sys.setdefaultencoding('utf-8') # required to convert to unicode

stop_words = set()
with open("stop_words_en.txt") as fd:
    for line in fd:
        stop_words.add(line.strip())

for line in sys.stdin:
    try:
        article_id, text = unicode(line.strip()).split('\t', 1)
    except ValueError as e:
        continue
    words = re.split("\W*\s+\W*", text, flags=re.UNICODE)
    word_stat = defaultdict(int)
    for word in words:
        word_stat[word.lower()] += 1
    for word, count in word_stat.iteritems():
        print >> sys.stderr, "reporter:counter:Wiki stats,Total words,%d" % count
        if word in stop_words:
            print >> sys.stderr, "reporter:counter:Wiki stats,Stop words,%d" % count
        print "%s\t%d" % (word, count)
