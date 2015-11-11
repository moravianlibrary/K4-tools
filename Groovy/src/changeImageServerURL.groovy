/**
 * Změna URL u monografie. Skript používá FedoraClient. Umožnujě změnit tyto URL: IMG_FULL, IMG_THUMB, IMG_ PREVIEW a tiles_url
 *
 * Created by kreplj on 11/9/15.
 */

@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')

import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import groovy.xml.XmlUtil

/**
 * SCRIPT SETTINGS
 */
def fedoraHost = 'http://fedoratest.mzk.cz/fedora'
def fedoraName = 'fedoraAdmin'
def fedoraPassword = 'fedoraAdmin'
def monographUuid = 'uuid:16dbc04a-3884-4385-9820-8ab4d2cca497'

FedoraCredentials credentials = new FedoraCredentials(fedoraHost, fedoraName, fedoraPassword);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

// get RELS-EXT for monograph
def response = fedoraClient.getDatastreamDissemination(monographUuid, "RELS-EXT").execute();
def xmlString = response.getEntity(String.class);

// get uuid from RELS-EXT xml
def rdfRDF = new XmlSlurper(false, false).parseText(xmlString)
def rdfResourceAttributes = rdfRDF.'rdf:Description'.hasPage.'@rdf:resource'
def attributesList = rdfResourceAttributes.list();

// for each uuid get its page. On every page change its image URLs
def i = 0
attributesList.each { attribute ->
    def uuid = attribute.text()
    uuid = uuid.replace('info:fedora/', '')
    println(i++ + ': Processing uuid: ' + uuid)

    modifyImageDataStreamUrl(uuid, 'IMG_FULL', fedoraClient)
    modifyImageDataStreamUrl(uuid, 'IMG_THUMB', fedoraClient)
    modifyImageDataStreamUrl(uuid, 'IMG_PREVIEW', fedoraClient)

    // modify tiles url
    def pageResponse = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute()
    def pageXmlString = pageResponse.getEntity(String.class);
    def pageXml = new XmlSlurper(false, false).parseText(pageXmlString)
    def tilesUrlNode = pageXml.'rdf:Description'.'tiles-url'
    def tilesUrl = tilesUrlNode.text()
    tilesUrl = fixMzkUrl(tilesUrl)
    tilesUrlNode.replaceNode {
        'tiles-url'(tilesUrl, xmlns: 'http://www.nsdl.org/ontologies/relationships#')
    }
    String editedXmlString = XmlUtil.serialize(pageXml)
    fedoraClient.modifyDatastream(uuid, 'RELS-EXT').versionable(false).content(editedXmlString).execute()
}

void modifyImageDataStreamUrl(uuid, dataStreamId, fedoraClient) {
    def imageResponse = fedoraClient.getDatastream(uuid, dataStreamId).execute()
    def imageXmlString = imageResponse.getEntity(String.class);
    def imageXml = new XmlSlurper(false, false).parseText(imageXmlString)
    def imageUrl = imageXml.dsLocation.text()
    imageUrl = fixMzkUrl(imageUrl)
    fedoraClient.modifyDatastream(uuid, dataStreamId).versionable(false).dsLocation(imageUrl).execute()
}

String fixMzkUrl(url) {
    url = url.replace('imageserver.mzk.cz', 'imageserver.mzk.cz/mzk03')
    return url.replace('2619298350/', '')
}

/*
RiSearch riSearch = new RiSearch('<info:fedora/uuid:16dbc04a-3884-4385-9820-8ab4d2cca497> <http://www.nsdl.org/ontologies/relationships#hasPage> *')
riSearch.type('triples')
riSearch.lang('spo')
riSearch.format('N-Triples')
riSearch.limit(1000)
riSearch.distinct(true)
riSearch.template('')
RiSearchResponse riSearchResponse = riSearch.execute()
def l = riSearchResponse.getEntity(String.class)
l.eachLine { line ->
    println(line)
}
*/