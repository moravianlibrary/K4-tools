/**
 * test if monograph has good urls in image server. Tested urls are tiles-url, img_full, img_preview, img_thumb
 *
 * test if monograph has good mimeType (image/jpeg) for img_full, img_preview, img_thumb. Without proper mimeType, images
 * won't be displayed in Kramerius
 *
 * Created by kreplj on 11/9/15.
 */

@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')

import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*

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

// for each uuid get its page. Test every page
def i = 0
attributesList.each { attribute ->
    def uuid = attribute.text()
    uuid = uuid.replace('info:fedora/', '')
    print('.')
    i++
    if (i % 100 == 0) {
        println()
    }
    // get tiles url
    def pageResponse = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute()
    def pageXmlString = pageResponse.getEntity(String.class);
    def pageXml = new XmlSlurper(false, false).parseText(pageXmlString)

    def tilesUrlNode = pageXml.'rdf:Description'.'kramerius:tiles-url'
    def tilesUrl = tilesUrlNode.text()
    if (tilesUrl == '') {
        tilesUrlNode = pageXml.'rdf:Description'.'tiles-url'
        tilesUrl = tilesUrlNode.text()
    }
    //System.out.println('    tilesUrl is: ' + tilesUrl)
    testDataStreamUrl(fedoraClient, uuid, 'IMG_FULL')
    testDataStreamUrl(fedoraClient, uuid, 'IMG_PREVIEW')
    testDataStreamUrl(fedoraClient, uuid, 'IMG_THUMB')
}

void testDataStreamUrl(fedoraClient, uuid, dataStreamId) {
    def imageResponse = fedoraClient.getDatastream(uuid, dataStreamId).execute()
    def imageXmlString = imageResponse.getEntity(String.class);
    def imageXml = new XmlSlurper(false, false).parseText(imageXmlString)
    def imageUrl = imageXml.dsLocation.text()
    if (!exists(imageUrl)) {
        System.out.println('Datastream ' + dataStreamId + ' on page ' + uuid + ' has bad url ' + imageUrl)
    }

    def mimeType = imageXml.dsMIME.text()
    if (mimeType != 'image/jpeg') {
        System.out.println('Page' + uuid + ' has wrong mime type: ' + mimeType)
    }
   // System.out.println('    Datastream ' + dataStreamId + ' has url: ' + imageUrl)
}

boolean exists(String URLName) {
    try {
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
        con.setRequestMethod("HEAD");
        return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    }
    catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


