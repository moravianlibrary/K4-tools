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

def FEDORA_URL = "http://***/fedora"
def FEDORA_USER = "fedoraAdmin"
def FEDORA_PASSWORD = "***"

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD);
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);
String fileContents = new File(args[0]).text

fileContents.eachLine() { uuid ->
    String xmlString = FedoraClient.getDatastreamDissemination(uuid, "DC").execute().getEntity(String)
    def dcXml = new XmlSlurper(false, false).parseText(xmlString)
    def dcModel = dcXml.'dc:type'.toString()

    def rdfRDFString = fedoraClient.getDatastreamDissemination(uuid, 'RELS-EXT').execute().getEntity(String)
    def rdfRDF = new XmlSlurper(false, true).parseText(rdfRDFString)
    def RdfModel = rdfRDF.Description.hasModel.'@rdf:resource'.toString()
    println "$uuid DC MODEL: $dcModel RELX-EXT MODEL: $RdfModel"
}
