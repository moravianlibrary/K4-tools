#!/usr/bin/python3.3
#-*- coding: utf-8 -*-

import json
import urllib2



api_vc = 'http://localhost/search/api/v5.0/search?q=document_type:%22manuscript%22&fq=keywords:%22archivn%C3%AD%20fondy%22&rows=500'
req= urllib2.Request(api_vc)
req.add_header("Accept", "application/json")
result=urllib2.urlopen(req)
jsonResult=json.load(result)

kramerius_prefix='http://kramerius.mzk.cz/search/handle/'
for item in jsonResult['response']['docs']:
  print((item['PID']))

