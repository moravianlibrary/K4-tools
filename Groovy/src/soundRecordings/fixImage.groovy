#!/usr/bin/env groovy
package soundRecordings

/**
 * this script fixes broken thumbnail images for tracks of sound_recordings
 */

@Grab('postgresql:postgresql:9.0-801.jdbc4')
@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')

@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '9.4-1205-jdbc42')

import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.*

/**
 * KRAMERIUS config
 */
def FEDORA_URL = 'http://10.2.0.75:8080/fedora'
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"

/**
 *  KRAMERIUS TEST config
 */
//def FEDORA_URL = 'http://fedoratest.mzk.cz/fedora'
//def FEDORA_USER = "fedoraAdmin"
//def FEDORA_PASSWORD = "*"

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)

def uuidPattern = "uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"

RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:soundrecording>')
RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(10000).distinct(true).template('').execute()
def soundRecordings = riSearchResponse.getEntity(String.class)

soundRecordings.eachLine { soundRecording ->
    def soundRecordingUuid = soundRecording.find(uuidPattern)
    String xmlString = FedoraClient.getDatastreamDissemination(soundRecordingUuid, "RELS-EXT").execute().getEntity(String)
    def soundRecordingXml = new XmlSlurper(false, false).parseText(xmlString)

    soundRecordingXml.'rdf:Description'.'**'.findAll { soundUnitNode ->
        if (soundUnitNode.name() == 'hasSoundUnit' || soundUnitNode.name() == 'kramerius:hasSoundUnit') {
            def rdfSoundUnitUuid = soundUnitNode.'@rdf:resource'
            def soundUnitUuid = rdfSoundUnitUuid.toString().find(uuidPattern)
            xmlString = FedoraClient.getDatastreamDissemination(soundUnitUuid, "RELS-EXT").execute().getEntity(String)
            def soundUnitXml = new XmlSlurper(false, false).parseText(xmlString)

            soundUnitXml.'rdf:Description'.'**'.findAll { trackNode ->
                if (trackNode.name() == 'containsTrack' || trackNode.name() == 'kramerius:containsTrack') {
                    def rdfTrackUuid = trackNode.'@rdf:resource'
                    def trackUuid = rdfTrackUuid.toString().find(uuidPattern)

                    def imageUrl = 'http://kramerius.mzk.cz/search/img/audioplayer/k4-player-thumb.png'
                    modifyImageDataStreamUrl(trackUuid, 'IMG_FULL', fedoraClient, imageUrl)
                    modifyImageDataStreamUrl(trackUuid, 'IMG_THUMB', fedoraClient, imageUrl)
                    modifyImageDataStreamUrl(trackUuid, 'IMG_PREVIEW', fedoraClient, imageUrl)

                }
            }
        }
    }
}

void modifyImageDataStreamUrl(uuid, dataStreamId, fedoraClient, newImageUrl) {
    def imageString = fedoraClient.getDatastream(uuid, dataStreamId).execute().getEntity(String);
    def imageXml = new XmlSlurper(false, false).parseText(imageString)
    def imageUrl = imageXml.dsLocation.text()
    if (imageUrl =~ /iris\.mzk\.cz/) {
        println "$uuid changing $imageUrl -> $newImageUrl"
        // there is probably a fedora bug, you cannot change mimeType with modifyDatastream() method. So purge, and recreate dataStream instead.
        fedoraClient.purgeDatastream(uuid, dataStreamId)
        fedoraClient.addDatastream(uuid, dataStreamId).controlGroup('R').mimeType('image/jpeg').versionable(false).dsLocation(newImageUrl).execute()
    }
}



