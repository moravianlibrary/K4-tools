#!/usr/bin/python
import urllib2
import json
from lxml import etree

api_vc = 'http://docker.mzk.cz/search/api/v5.0/search?q=collection%3Avc\%3A9784fefa-4ba8-4d6d-a4fc-85174d12aa23&fq=document_type:periodicalvolume&start=0&rows=500'
req= urllib2.Request(api_vc)
req.add_header("Accept", "application/json")
result=urllib2.urlopen(req)
jsonResult=json.load(result)

kramerius_prefix='http://kramerius.mzk.cz/search/handle/'
root= etree.Element('periodical')

for item in jsonResult['response']['docs']:
  
  itemelement = etree.Element('item')
  dctreeroot = etree.parse('http://docker.mzk.cz/search/api/v5.0/item/'+ item['root_pid'] +'/streams/DC'
);
  rootItem = etree.Element('root')
  rootItem.append(dctreeroot.getroot())
  periodicalVolume = etree.Element('periodicalVolume')
  dctree = etree.parse('http://docker.mzk.cz/search/api/v5.0/item/'+ item['PID'] +'/streams/DC')
  periodicalVolume.append(dctree.getroot())
  
  url = etree.Element('url')
  url.text = 'http://kramerius.mzk.cz/search/handle/' + item['PID']  
  thumbnail = etree.Element('thumbnail')
  thumbnail.text = 'http://docker.mzk.cz/search/api/v5.0/item/' + item['PID'] + '/thumb'
 
  itemelement.append(url)
  itemelement.append(thumbnail)
  itemelement.append(rootItem)
  itemelement.append(periodicalVolume)
  root.append(itemelement)

print etree.tostring(root, pretty_print=True)
