#!/usr/bin/env groovy

/**
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
import groovy.xml.XmlUtil

@GrabResolver(name = 'mzkrepo', root = 'http://ftp-devel.mzk.cz/mvnrepo/')
@Grab('cz.mzk.k5.api:K5-API:1.0')
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory
import cz.mzk.k5.api.remote.ProcessRemoteApi

def CHANGE_TO_MODEL = "model:***"

def FEDORA_URL = "http://***/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "***"
def KRAMERIUS_URL = "***.mzk.cz"
def KRAMERIUS_USER = "***"
def KRAMERIUS_PASWORD = "***"


FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(KRAMERIUS_URL, KRAMERIUS_USER, KRAMERIUS_PASWORD);

String fileContents = new File(args[0]).text

fileContents.eachLine() { uuid ->
    String xmlString = FedoraClient.getDatastreamDissemination(uuid, "DC").execute().getEntity(String)
    def dcXml = new XmlSlurper(false, false).parseText(xmlString)
    def dcOldModel = dcXml.'dc:type'.toString()
    if (dcOldModel != CHANGE_TO_MODEL) {
        dcXml.'dc:type'.replaceNode {
            'dc:type'(CHANGE_TO_MODEL)
        }
        String editedXmlString = XmlUtil.serialize(dcXml)
        fedoraClient.modifyDatastream(uuid, "DC").content(editedXmlString).execute();

        xmlString = FedoraClient.getDatastreamDissemination(uuid, "DC").execute().getEntity(String)
        dcXml = new XmlSlurper(false, false).parseText(xmlString)
        println "$uuid\nIN DC: $dcOldModel CHANGED TO ${dcXml.'dc:type'}"
    } else {
        println "$uuid -- skipping modification DC of has already $dcOldModel"
    }

    // change model in RELS-EXT
    def rdfRDFString = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute().getEntity(String)
    def rdfRDF = new XmlSlurper(false, true).parseText(rdfRDFString)
    def oldRdfModel = rdfRDF.Description.hasModel.'@rdf:resource'.toString()
    def newRdfModel = "info:fedora/$CHANGE_TO_MODEL"
    if (oldRdfModel != newRdfModel) {
        rdfRDF.Description.hasModel.'@rdf:resource' = newRdfModel.toString()
        def rdfRDFEditedString = XmlUtil.serialize(rdfRDF)
        fedoraClient.modifyDatastream(uuid, "RELS-EXT").content(rdfRDFEditedString).execute();

        rdfRDFString = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute().getEntity(String)
        rdfRDF = new XmlSlurper(false, true).parseText(rdfRDFString)
        println "IN RELS-EXT: $oldRdfModel CHANGED TO ${rdfRDF.Description.hasModel.'@rdf:resource'}"
    } else {
        println "$uuid -- skipping modification RELS-EXT has already $oldRdfModel"
    }

    if (dcOldModel != CHANGE_TO_MODEL || oldRdfModel != newRdfModel) {
        println "...reindexing..."
        remoteApi.reindex(uuid)
    }
    println ""
}
