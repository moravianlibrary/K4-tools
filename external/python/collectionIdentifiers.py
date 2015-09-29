#!/usr/bin/python
import urllib2
import json

api_vc = 'http://docker.mzk.cz/search/api/v5.0/search?q=collection%3Avc\%3A9784fefa-4ba8-4d6d-a4fc-85174d12aa23&fq=document_type:periodicalvolume&start=0&rows=500'
req= urllib2.Request(api_vc)
req.add_header("Accept", "application/json")
result=urllib2.urlopen(req)
jsonResult=json.load(result)

kramerius_prefix='http://kramerius.mzk.cz/search/handle/'
for item in jsonResult['response']['docs']:
  print  kramerius_prefix + item['PID'] + "\t" +  kramerius_prefix + item['root_pid']


