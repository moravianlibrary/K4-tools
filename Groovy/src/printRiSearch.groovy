#!/usr/bin/env groovy
@Grab('postgresql:postgresql:9.0-801.jdbc4')
@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import groovy.sql.Sql
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.*

def FEDORA_URL = 'http://fedoratest.mzk.cz/fedora'
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"
FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)


// risearch dotaz na fedory na vsechny model periodicalvolume
RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:soundrecording>')
RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(10000).distinct(true).template('').execute()
def soundRecordings = riSearchResponse.getEntity(String.class)

// odpoved z risearch zpracovame po radcich
soundRecordings.eachLine { soundRecording ->
    // z jednotlivych radku parsujeme uuid
    def soundRecordingUuid = soundRecording.find(/uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)
    println "SOUND_RECORDING: $soundRecordingUuid";

    riSearch = new RiSearch("<info:fedora/$soundRecordingUuid> <http://www.nsdl.org/ontologies/relationships#hasSoundUnit> *")
    riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(10000).distinct(true).template('').execute()
    def soundUnits = riSearchResponse.getEntity(String.class)
    soundUnits.eachLine { soundUnit ->
        def soundUnitUuid = soundUnit.findAll(/uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)[1]
        println "\tcontains SOUND_UNIT: ${soundUnitUuid}"

        riSearch = new RiSearch("<info:fedora/$soundUnitUuid> <http://www.nsdl.org/ontologies/relationships#containsTrack> *")
        riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(10000).distinct(true).template('').execute()
        def tracks = riSearchResponse.getEntity(String.class)
        tracks.eachLine { track ->
            def trackUuid = track.findAll(/uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/)[0]
            println "\t\tcontains TRACK: $trackUuid";
        }
    }
    println ""
}