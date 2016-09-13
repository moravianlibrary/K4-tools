#!/usr/bin/env groovy

/**
 * https://github.com/moravianlibrary/kramerius/issues/125
 * find if periodical has images embedded in fedora (images are not stored in imageserver)
 *
 * USAGE
 *
 * $ groovy periodicals.groovy
 */

@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.RiSearchResponse

def FEDORA_URL = System.getenv()['FEDORA_URL']
def FEDORA_USER = System.getenv()['FEDORA_USER']
def FEDORA_PASSWORD = System.getenv()['FEDORA_PASSWORD']

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD)
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)

def ln = System.getProperty('line.separator')
def outputFile = new File("out.txt")
def volumeMissingFile = new File("volumeMissing.txt")
def doesNotHaveIssueFile = new File("doesNotHaveIssue.txt")
outputFile.write("") // some content may exist from previous run, erase the contents of file by writing ""
volumeMissingFile.write("")
doesNotHaveIssueFile.write("")

def getRiSearchResponse = { String query ->
    def RiSearch riSearch = new RiSearch(query)
    def RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('RDF/XML').distinct(true).template('').execute()
    def response = riSearchResponse.getEntity(String.class)
    return new XmlSlurper(false, true).parseText(response)
}

def lineCount = 1
def periodicalsXml = getRiSearchResponse("* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:periodical>")
println "Number of documents: ${periodicalsXml.Description.size()}"
periodicalsXml.Description.each { periodicalUuidNode ->
    print "${lineCount++} "

    def periodicalUuid = periodicalUuidNode."@rdf:about".toString()
//    println "periodical: $periodicalUuid"
    def relsExtXml = getRiSearchResponse("<$periodicalUuid> <http://www.nsdl.org/ontologies/relationships#hasVolume> *")
    def volumeUuid = relsExtXml.Description.hasVolume[0].'@rdf:resource'.toString()
    if(volumeUuid.isEmpty()) {
        periodicalUuid = periodicalUuid.replace("info:fedora/", "")
        volumeMissingFile << "periodical: $periodicalUuid does not have volume $ln"
        volumeMissingFile << "http://kramerius.mzk.cz/search/handle/$periodicalUuid $ln"
        volumeMissingFile << "http://fedora.dk-back.infra.mzk.cz/fedora/get/$periodicalUuid $ln$ln"
    } else {
//        println "volume: $volumeUuid"
        def itemsXml = getRiSearchResponse("<$volumeUuid> <http://www.nsdl.org/ontologies/relationships#hasItem> *")
        def itemUuid = itemsXml.Description.hasItem[0].'@rdf:resource'.toString()
//        println "item: $itemUuid"
        if (itemUuid.isEmpty()) {
            volumeUuid = volumeUuid.replace("info:fedora/", "")
            doesNotHaveIssueFile << "volume: $volumeUuid does not have issues $ln"
            doesNotHaveIssueFile << "http://kramerius.mzk.cz/search/handle/$volumeUuid $ln"
            doesNotHaveIssueFile << "http://fedora.dk-back.infra.mzk.cz/fedora/get/$volumeUuid $ln$ln"
        } else {
            def pagesXml = getRiSearchResponse("<$itemUuid> <http://www.nsdl.org/ontologies/relationships#hasPage> *")
            def pageUuid = pagesXml.Description.hasPage[0].'@rdf:resource'.toString()
            pageUuid = pageUuid.replace("info:fedora/", "")
            def dsLocation = FedoraClient.getDatastream(pageUuid, "IMG_FULL").execute().getDatastreamProfile().getDsLocationType()
//            println "page: $pageUuid$ln"

            if (dsLocation.equals("INTERNAL_ID")) {
                outputFile << "${periodicalUuid.replace("info:fedora/", "")}$ln"
            }
        }
    }
}