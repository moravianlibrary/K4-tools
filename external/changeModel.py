#!/usr/bin/python
import base64
import fileinput
import json
import urllib2
import eulfedora
from eulfedora.server import Repository
from lxml import etree
from eulfedora.util import RequestFailed
import time

fedora_url = *
fedora_username = *
fedora_password = *
kramerius_username = *
kramerius_password = *

def change_model(uuid, model):
    eulfedora = Repository(root=fedora_url, username=fedora_username, password=fedora_password)
    datastreamDc, datastreamDc_url = eulfedora.api.getDatastreamDissemination(pid=uuid, dsID='DC')
    dc = etree.fromstring(datastreamDc)
    dc_type = dc.xpath('//dc:type',  namespaces={'dc': 'http://purl.org/dc/elements/1.1/'})
    dc_type[0].text=model
    datastream_rels_ext, datastream_rels_ext_url = eulfedora.api.getDatastreamDissemination(pid=uuid, dsID='RELS-EXT')
    rels_ext = etree.fromstring(datastream_rels_ext)
    rdf_ns = '{http://www.w3.org/1999/02/22-rdf-syntax-ns#}'
    rels_type = rels_ext.xpath('//model:hasModel', namespaces={'model': 'info:fedora/fedora-system:def/model#'})
    rels_type[0].attrib[rdf_ns+'resource'] ='info:fedora/'+model
    try:
        eulfedora.api.modifyDatastream(pid=uuid, mimeType='text/xml', dsID='DC', content=etree.tostring(dc))
        eulfedora.api.modifyDatastream(pid=uuid, mimeType='application/rdf+xml', dsID='RELS-EXT', content=etree.tostring(rels_ext))
    except RequestFailed as e:
      print e.args

def reindex(uuid):
    data = {"parameters": ["fromKrameriusModel", uuid]}
    data_json = json.dumps(data)
    base64string = base64.encodestring('%s:%s' % (kramerius_user, kramerius_password)).replace('\n', '')
    req = urllib2.Request('http://kramerius.mzk.cz/search/api/v4.6/processes?def=reindex', data=data_json)
    req.add_header("Content-type", 'application/json')
    req.add_header("Authorization", "Basic %s" % base64string)
    urllib2.urlopen(req)

for line in fileinput.input():
     uuid = line.rstrip()
     print('change model  ' + uuid)
     change_model(uuid, 'model:archive')
     print('reindex ' + uuid)
     reindex(uuid)
     time.sleep(5)






