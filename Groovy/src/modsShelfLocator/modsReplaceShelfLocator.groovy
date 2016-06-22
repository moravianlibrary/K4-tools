#!/usr/bin/env groovy
package modsShelfLocator

/**
 * https://github.com/moravianlibrary/kramerius/issues/91
 * in MODS finds and replace substring of shelf locator (in issue#91 substring='BOA001')
 *
 * USAGE
 *
 * $ groovy modsShelfLocator.modsReplaceShelfLocator.groovy
 */

@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.RiSearchResponse
import groovy.xml.XmlUtil

@GrabResolver(name = 'mzkrepo', root = 'http://ftp-devel.mzk.cz/mvnrepo/')
@Grab('cz.mzk.k5.api:K5-API:1.0')
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory
import cz.mzk.k5.api.remote.ProcessRemoteApi

//def FEDORA_URL = "http://fedoratest.mzk.cz/fedora"
//def FEDORA_USER = "fedoraAdmin"
//def FEDORA_PASSWORD = "*"
//def KRAMERIUS_URL = "krameriusTest.mzk.cz"

def FEDORA_URL = "http://10.2.0.75:8080/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"
def KRAMERIUS_URL = "kramerius.mzk.cz"

def scriptOutputFile = new File("src/modsReplaceShelfLocator-output.txt")
def xmlOutputFile = new File("src/modsReplaceShelfLocator-xml-output.txt")
// some content may exist from previous script run, erase the contents of file by writing ""
scriptOutputFile.write("")
xmlOutputFile.write("")
def ln = System.getProperty('line.separator')

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD)
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)

def uuidPattern = /uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|uuid:[0-9a-f]{32}/
def lineCount = 0

def MONOGRAPH = 'monograph'
def PERIODICAL = 'periodical'
def MAP = 'map'
def ARCHIVE = 'archive'
def MANUSCRIPT = 'manuscript'
def GRAPHIC = 'graphic'
def SOUND_RECORDING = 'soundrecording'
def SHEET_MUSIC = 'sheetmusic'

RiSearch riSearch = new RiSearch("* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:$MONOGRAPH>")
RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').distinct(true).template('').execute()
def input = riSearchResponse.getEntity(String.class)
String[] lines = input.split("\r\n|\r|\n");
println "Query done, number of documents: $lines.length"

input.eachLine() { line ->
    print "$lineCount "
    def uuid = line.find(uuidPattern)
    def xmlString
    try {
        xmlString = FedoraClient.getDatastreamDissemination(uuid, "BIBLIO_MODS").execute().getEntity(String)
    } catch (Exception e) {
//            println "$uuid NOT IN FEDORA\n"
        return
    }
    def modsXml = new XmlSlurper(false, true).parseText(xmlString)
    def shelfLocatorNode = modsXml.mods.location.shelfLocator[0]
    def shelfLocatorText = shelfLocatorNode.text()
    def newShelfLocatorText = shelfLocatorText.replace('BOA001', '')

    if (shelfLocatorText != newShelfLocatorText) {
        def message = "REPARING line: $lineCount, $FEDORA_URL/get/$uuid http://$KRAMERIUS_URL/search/handle/$uuid\n" +
                "OLD shelfLocator: $shelfLocatorText\n" +
                "NEW shelfLocator: $newShelfLocatorText\n"
        scriptOutputFile << "$message$ln"

        shelfLocatorNode.replaceBody(newShelfLocatorText)
        xmlOutputFile << "${XmlUtil.serialize(modsXml)}$ln"
        //
        //
         fedoraClient.modifyDatastream(uuid, "BIBLIO_MODS").content(XmlUtil.serialize(modsXml)).execute();
        //
        //
    }
    lineCount++
}