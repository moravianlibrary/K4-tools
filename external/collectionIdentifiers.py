#!/usr/bin/python
import urllib2
import json

api_vc = 'http://krameriusndktest.mzk.cz/search/api/v5.0/search?q=collection%3Avc\%3A54940333-8f1d-4d24-ba3b-ac042a38a949&fq=document_type:monograph&start=0&rows=500'
req= urllib2.Request(api_vc)
req.add_header("Accept", "application/json")
result=urllib2.urlopen(req)
jsonResult=json.load(result)

kramerius_prefix='http://krameriusndktest.mzk.cz/search/handle/'
for item in jsonResult['response']['docs']:
  print kramerius_prefix + item['root_pid']


