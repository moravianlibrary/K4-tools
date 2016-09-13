#!/usr/bin/env groovy

/**
 * https://github.com/moravianlibrary/kramerius/issues/125
 * find if every document types but periodical has images embedded in fedora (images are not stored in imageserver)
 *
 * USAGE
 *
 * $ groovy otherDocuments.groovy
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
def errorFile = new File("error.txt")
outputFile.write("") // some content may exist from previous run, erase the contents of file by writing ""
errorFile.write("")

def getRiSearchResponse = { String query ->
    def RiSearch riSearch = new RiSearch(query)
    def RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('RDF/XML').distinct(true).template('').execute()
    def response = riSearchResponse.getEntity(String.class)
    return new XmlSlurper(false, true).parseText(response)
}

def dsDisseminationXml = { String uuid, String dataStream ->
    def response = FedoraClient.getDatastreamDissemination(uuid, dataStream).execute().getEntity(String)
    return new XmlSlurper(false, true).parseText(response)
}

//['map', 'archive', 'manuscript', 'graphic', 'sheetmusic'].each { model ->
['monograph'].each { model ->
    def documentXml = getRiSearchResponse("* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:$model>")
//    def documentXml = getRiSearchResponse("<info:fedora/uuid:7fe72533-a974-11e0-a5e1-0050569d679d> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:sheetmusic>")
    println "Number of documents: ${documentXml.Description.size()} with model $model"
    outputFile << "model: $model $ln"
    def lineCount = 1
    documentXml.Description.each { documentUuidNode ->
        print "${lineCount++} "
        def documentUuid = documentUuidNode."@rdf:about".toString()
        def pagesXml = getRiSearchResponse("<$documentUuid> <http://www.nsdl.org/ontologies/relationships#hasPage> *")
        def pageUuid = pagesXml.Description.hasPage[0].'@rdf:resource'.toString()
        documentUuid = documentUuid.replace("info:fedora/", "")
        pageUuid = pageUuid.replace("info:fedora/", "")
        println "$documentUuid -> $pageUuid"
        if (pageUuid.isEmpty()) {
            errorFile << "$documentUuid does not page $ln"
            errorFile << "http://kramerius.mzk.cz/search/handle/$documentUuid $ln"
            errorFile << "http://fedora.dk-back.infra.mzk.cz/fedora/get/$documentUuid $ln$ln"


        } else {
            def imageDataStreamProfile = null
            try {
                imageDataStreamProfile = FedoraClient.getDatastream(pageUuid, "IMG_FULL").execute().getDatastreamProfile()
            } catch (Exception e) {
                errorFile << """
Something is wrong with pages of document: $documentUuid
http://kramerius.mzk.cz/search/handle/$documentUuid
http://fedora.dk-back.infra.mzk.cz/fedora/get/$documentUuid $ln
"""
            }
            if (imageDataStreamProfile && imageDataStreamProfile.getDsLocationType().equals("INTERNAL_ID")) {
                def modsNode = dsDisseminationXml(documentUuid, "BIBLIO_MODS").mods
                outputFile << "${modsNode.titleInfo.title.toString()} | "
                outputFile << "${imageDataStreamProfile.getDsMIME()} | "
                outputFile << "${modsNode.identifier} | "
                outputFile << "http://kramerius.mzk.cz/search/handle/$documentUuid $ln"
            }
        }
    }
}