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
def fedoraHost = 'http://fedora.mzk.cz/fedora'
def fedoraName = 'fedoraAdmin'
def fedoraPassword = 'flouTMouSe71'
def monographUuid = 'uuid:f9f665d1-e6c4-486a-afa7-016c5f2a1466'

FedoraCredentials credentials = new FedoraCredentials(fedoraHost, fedoraName, fedoraPassword);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

// get RELS-EXT for monograph
def response = fedoraClient.getDatastreamDissemination(monographUuid, "RELS-EXT").execute();
def xmlString = response.getEntity(String.class);

// get uuid from RELS-EXT xml
def rdfRDF = new XmlSlurper(false, false).parseText(xmlString)
def rdfResourceAttributes = rdfRDF.'rdf:Description'.'kramerius:hasPage'.'@rdf:resource'
def attributesList = rdfResourceAttributes.list();

// for each uuid get its page. On every page change its image URLs
def i = 0
attributesList.each { attribute ->
    def uuid = attribute.text()
    uuid = uuid.replace('info:fedora/', '')
    println(i++ + ': Processing ' + uuid)

    // modify tiles url
    def pageResponse = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute()
    def pageXmlString = pageResponse.getEntity(String.class);
    def pageXml = new XmlSlurper(false, false).parseText(pageXmlString)

    def tilesUrlNode = pageXml.'rdf:Description'.'kramerius:tiles-url'
    def tilesUrl = tilesUrlNode.text()
    if (tilesUrl == '') {
        tilesUrlNode = pageXml.'rdf:Description'.'tiles-url'
        tilesUrl = tilesUrlNode.text()
    }
    def fixedTilesUrl = fixMzkUrl(tilesUrl)
    if (tilesUrl != fixedTilesUrl) {
        tilesUrlNode.replaceNode {
            'tiles-url'(fixedTilesUrl, xmlns: 'http://www.nsdl.org/ontologies/relationships#')
        }
    }
    String editedXmlString = XmlUtil.serialize(pageXml)
    fedoraClient.modifyDatastream(uuid, 'RELS-EXT').versionable(false).content(editedXmlString).execute()

    // modify IMG_FULL, IMG_THUMB, IMG_PREVIEW dataStreams
    modifyImageDataStreamUrl(uuid, 'IMG_FULL', fedoraClient, fixedTilesUrl)
    modifyImageDataStreamUrl(uuid, 'IMG_THUMB', fedoraClient, fixedTilesUrl)
    modifyImageDataStreamUrl(uuid, 'IMG_PREVIEW', fedoraClient, fixedTilesUrl)
}

void modifyImageDataStreamUrl(uuid, dataStreamId, fedoraClient, fixedTilesUrl) {
    try {
        def imageResponse = fedoraClient.getDatastream(uuid, dataStreamId).execute()
        def imageXmlString = imageResponse.getEntity(String.class);
        def imageXml = new XmlSlurper(false, false).parseText(imageXmlString)
        def imageUrl = imageXml.dsLocation.text()
        def fixedImageUrl = fixMzkUrl(imageUrl)
//        if (fixedImageUrl != imageUrl) {
            // there is probably a fedora bug, you cannot change mimeType with modifyDatastream() method. So purge, and recreate ds instead.
            fedoraClient.purgeDatastream(uuid, dataStreamId)
            fedoraClient.addDatastream(uuid, dataStreamId).controlGroup('R').mimeType('image/jpeg').versionable(false).dsLocation(fixedImageUrl).execute()
//        }
    } catch (FedoraClientException e) {
        System.out.println('Could not get datastream: ' + dataStreamId + ' of object: ' + uuid)

        def fullImageUrl
        switch (dataStreamId) {
            case 'IMG_FULL':
                fullImageUrl = fixedTilesUrl + '/big.jpg'
                break;
            case 'IMG_THUMB':
                fullImageUrl = fixedTilesUrl + '/thumb.jpg'
                break;
            default:
                fullImageUrl = fixedTilesUrl + '/preview.jpg'
        }
        System.out.println('New image url is: ' + fullImageUrl + ' ... adding new datastream')
        fedoraClient.addDatastream(uuid, dataStreamId).controlGroup('R').mimeType('image/jpeg').versionable(false).dsLocation(fullImageUrl).execute()
    }
}

String fixMzkUrl(url) {
//    url = url.replaceFirst('2619298350', 'mzk03')
//    return url.replace('2619298350/', '')

    if (url.contains('mzk03')) {
        return url;
    } else {
        return url.replaceFirst('2619298437', 'mzk03')
    }

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