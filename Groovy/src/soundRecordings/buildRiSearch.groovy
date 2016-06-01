#!/usr/bin/env groovy
package soundRecordings

/**
 * this script loads data about sound_recordings from fedora
 * then from those data it recreates records in ritriples database
 */

@Grab('postgresql:postgresql:9.0-801.jdbc4')
@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')

@GrabConfig(systemClassLoader=true)
@Grab(group='org.postgresql', module='postgresql', version='9.4-1205-jdbc42')

import groovy.sql.Sql
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.*

/**
 * from tmap krameriusTest
 * t46 contains <http://www.nsdl.org/ontologies/relationships#hasSoundUnit>
 * t47 contains <http://www.nsdl.org/ontologies/relationships#containsTrack>
 *
 * from tmap kramerius
 * t45 contains <http://www.nsdl.org/ontologies/relationships#hasSoundUnit>
 * t44 contains <http://www.nsdl.org/ontologies/relationships#containsTrack>
 */
def HAS_SOUND_UNIT_TABLE = 't46'
def HAS_TRACK_TABLE = 't47'

/**
 * KRAMERIUS config
 */
//def dbUrl = "jdbc:postgresql://localhost:5432/ritriples"
//def dbUser = "fedoraadmin"
//def dbPassword = "*"
//def dbDriver = "org.postgresql.Driver"
//def FEDORA_URL = 'http://10.2.0.75:8080/fedora'
//def FEDORA_USER = "fedoraAdmin"
//def FEDORA_PASSWORD = "*"

/** KRAMERIUS TEST config
 *
 * pro pristup z localhostu do riTripleS je nutne otevrit:
 * $ ssh -L 5434:localhost:5434 krameriustest.mzk.cz
 * protoze docker kontejner kramerius_backend_riTriples ma zverejneny port 5434->5432
 */
def dbUrl = "jdbc:postgresql://localhost:5434/riTriples"
def dbUser = "fedoraAdmin"
def dbPassword = "*"
def dbDriver = "org.postgresql.Driver"
def FEDORA_URL = 'http://fedoratest.mzk.cz/fedora'
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)
def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, dbDriver)
sql.execute "DELETE FROM $HAS_SOUND_UNIT_TABLE".toString()
sql.execute "DELETE FROM $HAS_TRACK_TABLE".toString()
def uuidPattern = "uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"

RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:soundrecording>')
RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(10000).distinct(true).template('').execute()
def soundRecordings = riSearchResponse.getEntity(String.class)

soundRecordings.eachLine { soundRecording ->
    def soundRecordingRiSearchObject = soundRecording.find("<info:fedora/$uuidPattern>")
    def soundRecordingUuid = soundRecording.find(uuidPattern)
    String xmlString = FedoraClient.getDatastreamDissemination(soundRecordingUuid, "RELS-EXT").execute().getEntity(String)
    def xml = new XmlSlurper(false, false).parseText(xmlString)

    xml.'rdf:Description'.'**'.findAll { soundUnitNode ->
        if (soundUnitNode.name() == 'hasSoundUnit' || soundUnitNode.name() == 'kramerius:hasSoundUnit') {
            def rdfSoundUnitUuid = soundUnitNode.'@rdf:resource'
            def soundUnitRiSearchObject = "<$rdfSoundUnitUuid>".toString()
            def soundUnitUuid = rdfSoundUnitUuid.toString().find(uuidPattern)
            sql.execute "insert into $HAS_SOUND_UNIT_TABLE (s, o) values (?, ?)", [soundRecordingRiSearchObject, soundUnitRiSearchObject]
            xmlString = FedoraClient.getDatastreamDissemination(soundUnitUuid, "RELS-EXT").execute().getEntity(String)
            def soundUnitXml = new XmlSlurper(false, false).parseText(xmlString)

            soundUnitXml.'rdf:Description'.'**'.findAll { trackNode ->
                if (trackNode.name() == 'containsTrack' || trackNode.name() == 'kramerius:containsTrack') {
                    def rdfTrackUuid = trackNode.'@rdf:resource'
                    def trackRiSearchObject = "<$rdfTrackUuid>".toString()
                    sql.execute "insert into $HAS_TRACK_TABLE (s, o) values (?, ?)", [soundUnitRiSearchObject, trackRiSearchObject]
                }
            }
        }
    }
}


