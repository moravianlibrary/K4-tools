#!/usr/bin/python

import urllib2, base64, sys
#sys
#re
#base64
#from urlparse import urlparse
import json
import datetime

apipoint_process = 'http://krameriusndktest.mzk.cz/search/api/v4.6/processes?state=PLANNED'
username = 'nagios'
password = 'W9PdTcj5'
base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')
req = urllib2.Request(apipoint_process)
req.add_header("Authorization", "Basic %s" % base64string)
result = urllib2.urlopen(req)
processes = json.load(result)
for process in processes:
  planned = datetime.datetime.strptime(process['planned'], '%m/%d/%Y %H:%M:%S:%f')
  delta = datetime.datetime.now() - planned
  if delta.seconds > 600:
    print "WARNING - a process has not been started for %s seconds." % delta.seconds
    sys.exit(1)
print "OK - processes running successfully."
sys.exit(0)

