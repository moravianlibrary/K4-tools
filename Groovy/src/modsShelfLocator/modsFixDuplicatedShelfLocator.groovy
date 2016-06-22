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
//def KRAMERIUS_URL = "krameriusTest.mzk.cz"

def FEDORA_URL = "http://10.2.0.75:8080/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"
def KRAMERIUS_URL = "kramerius.mzk.cz"

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
//RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:map>')
//RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:archive>')
//RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:manuscript>')
//RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:soundrecording>')
//RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:sheetmusic>')
RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:monograph>')
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
    def shelfLocator = modsXml.mods.location.shelfLocator
    def physicalLocation = modsXml.mods.location.physicalLocation.toString()
    if (physicalLocation.equals("BOA001")) {
        def size = shelfLocator.size()
        if (size == 0) {
            return
        } else if (size > 1) {
            def newShelfLocator = shelfLocator[0].toString()
            def message = "$uuid, \n$FEDORA_URL/get/$uuid \nhttp://$KRAMERIUS_URL/search/handle/$uuid\n" +
                    "old_locators: '${shelfLocator[0]}' "
            for (def i = 1; i < shelfLocator.size(); i++) {
                message += "| '${shelfLocator[i]}'"
                def shelfLocatorString = shelfLocator[i].toString()
                if (shelfLocatorString != null && !shelfLocatorString.isEmpty() && shelfLocatorString.find(/^[0-9]{1,4}$/)) {
                    newShelfLocator += ",${shelfLocator[i]}"
                } else {
                    fileOutput2 << "NOT_REPARING_NO: $message\n\n"
                    return
                }
            }
            fileOutput2 << "REPARING \n$message \nNEW_LOCATOR: $newShelfLocator\n\n"
            for (i = 1; i < shelfLocator.size(); i++) {
                shelfLocator[i].replaceNode {}
            }
            shelfLocator[0].replaceBody(newShelfLocator)

            fileOutput << XmlUtil.serialize(modsXml)
            //
            //
//            fedoraClient.modifyDatastream(uuid, "BIBLIO_MODS").content(XmlUtil.serialize(modsXml)).execute();
            //
            //
        }
    }
    lineCount++
}