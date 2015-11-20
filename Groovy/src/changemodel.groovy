#!/usr/bin/env groovy
@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.response.*
import com.yourmediashelf.fedora.client.request.*
import groovy.xml.XmlUtil

@GrabResolver(name = 'mzkrepo', root = 'http://ftp-devel.mzk.cz/mvnrepo/')
@Grab('cz.mzk.k5.api:K5-API:1.0')
import cz.mzk.k5.api.remote.KrameriusProcessRemoteApiFactory
import cz.mzk.k5.api.remote.ProcessRemoteApi

def CHANGE_TO_MODEL = "model:*"
def FEDORA_URL = "http://fedora*/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "*"
def KRAMERIUS_URL = "kramerius.*.cz"
def KRAMERIUS_USER = "krameriusAdmin"
def KRAMERIUS_PASWORD = "*"


FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

ProcessRemoteApi remoteApi = KrameriusProcessRemoteApiFactory.getProcessRemoteApi(KRAMERIUS_URL, KRAMERIUS_USER, KRAMERIUS_PASWORD);


System.in.eachLine() { line ->
    def uuid = line

    println line + ": mění se model na " + CHANGE_TO_MODEL
    String xmlString = FedoraClient.getDatastreamDissemination(uuid, "DC").execute().getEntity(String)
    def xml = new XmlSlurper(false, false).parseText(xmlString)

    xml.'dc:type'.replaceNode {
        'dc:type'(CHANGE_TO_MODEL)
    }
    String editedXmlString = XmlUtil.serialize(xml)
    fedoraClient.modifyDatastream(uuid, "DC").content(editedXmlString).execute();

    // change model in RELS-EXT
    def rdfRDFString = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute().getEntity(String)
    def rdfRDF = new XmlSlurper(false, false).parseText(rdfRDFString)
    rdfRDF.'rdf:Description'.hasModel.'@rdf:resource' = 'info:fedora/' + CHANGE_TO_MODEL
    def rdfRDFEditedString = XmlUtil.serialize(rdfRDF)
    fedoraClient.modifyDatastream(uuid, "RELS-EXT").content(rdfRDFEditedString).execute();


    println line + ": plánuje se reindexace"
    remoteApi.reindexRecursive(uuid)
}
