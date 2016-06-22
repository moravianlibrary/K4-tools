#!/usr/bin/env groovy
package modsShelfLocator

/**
 * https://github.com/moravianlibrary/kramerius/issues/95
 * in MODS finds and fixes duplicated shelf locator
 *
 * USAGE
 *
 * UUIDs are in <file> (every UUID is on one line)
 * $ groovy changeModel.groovy <file>
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

def FEDORA_URL = "http://10.2.0.75:8080/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"

def fileOutput = new File("src/duplicateShelfLocator - output.txt");
def fileOutput2 = new File("src/duplicateShelfLocator - output2.txt");
fileOutput2.write("")
fileOutput.write("")

def ln = System.getProperty('line.separator')

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

def uuidPattern = /uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}|uuid:[0-9a-f]{32}/
def lineCount = 0

//map archive manuscript graphic
RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:graphic>')
RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').distinct(true).template('').execute()
def input = riSearchResponse.getEntity(String.class)
String[] lines = input.split("\r\n|\r|\n");
println "Query done, number of documents: $lines.length"

input.eachLine() { line ->
    print "$lineCount "
    def uuid = line.find(uuidPattern)
    // find page of document to know what base it is from. We want process only our MZK* bases
    riSearch = new RiSearch("<info:fedora/$uuid> <http://www.nsdl.org/ontologies/relationships#hasPage> *")
    riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').limit(1).distinct(true).template('').execute()
    def hasPageTriplet = riSearchResponse.getEntity(String.class)
    def pageUuid = hasPageTriplet.findAll(uuidPattern)[1]
    def pageRelsExt
    try {
        xmlString = FedoraClient.getDatastream(pageUuid, "IMG_FULL").execute().getEntity(String)
    } catch (Exception e) {
//        println "page $pageUuid NOT IN FEDORA\n"
        return
    }
    pageRelsExt = new XmlSlurper(false, true).parseText(xmlString)
    // omit all NDK documents, process only documents from MZK* bases
    if (!pageRelsExt.dsLocation.text().find(/NDK/)) {
        def xmlString
        try {
            xmlString = FedoraClient.getDatastreamDissemination(uuid, "BIBLIO_MODS").execute().getEntity(String)
        } catch (Exception e) {
//            println "$uuid NOT IN FEDORA\n"
            return
        }
        def modsXml = new XmlSlurper(false, true).parseText(xmlString)
        def shelfLocator = modsXml.mods.location.shelfLocator
        def size = shelfLocator.size()

        def first, second, third
        if (size > 1) {
            if (size == 2) {
                first = shelfLocator[0].toString()
                second = shelfLocator[1].toString()
                newShelfLocator = "$first,$second"
                fileOutput2 << "$uuid FIRST: $first SECOND: $second$ln"

                shelfLocator[1].replaceNode {}
                shelfLocator[0].replaceBody(newShelfLocator)
                fileOutput << XmlUtil.serialize(modsXml)
            } else if (size == 3) {
                first = shelfLocator[0].toString()
                second = shelfLocator[1].toString()
                third = shelfLocator[2].toString()
                newShelfLocator = "$first,$second,$third"
                fileOutput2 << "$uuid FIRST: $first SECOND: $second, THIRD: $third$ln"

                shelfLocator[1].replaceNode {}
                shelfLocator[2].replaceNode {}
                shelfLocator[0].replaceBody(newShelfLocator)
                fileOutput << XmlUtil.serialize(modsXml)
            }
            //
            //
//             fedoraClient.modifyDatastream(uuid, "BIBLIO_MODS").content(XmlUtil.serialize(modsXml)).execute();
            //
            //
        } else {
            first = shelfLocator[0].toString()
            fileOutput2 << "FIRST: $first$ln"
        }
   }
    lineCount++
}