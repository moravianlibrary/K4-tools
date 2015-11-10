/**
 * Created by kreplj on 10/26/15.
 * Usage: changemodel uuid model
 *
 * Example:
 *        changemodel uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22 model:sheetmusic
 */

@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')

import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.response.*
import com.yourmediashelf.fedora.client.request.*
import groovy.xml.XmlUtil

def config = new ConfigSlurper().parse(new File('Groovy/src/config.groovy').toURI().toURL());

// parse arguments and print usage (if called without any arguments)
def cli = new CliBuilder(usage: 'changemodel uuid model')
def options = cli.parse(args)
def args = options.arguments()

if (args.isEmpty()) {
    cli.usage()
    return
}

FedoraCredentials credentials = new FedoraCredentials('http://fedoratest.mzk.cz/fedora', "fedoraAdmin", "fedoraAdmin");
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

String xmlString = getXml()
def xml = new XmlSlurper(false, false).parseText(xmlString)

xml.'dc:type'.replaceNode {
    'dc:type'(args[1])
}
String editedXmlString = XmlUtil.serialize(xml)
fedoraClient.modifyDatastream(args[0], "DC").content(editedXmlString).execute();

println('Printing modified xml from Fedora:\n\n')
println(getXml())

/**
 * get Xml String from Fedora
 *
 * @return String xml
 */
String getXml() {
    FedoraResponse r = FedoraClient.getDatastreamDissemination(args[0], "DC").execute();
    return r.getEntity(String.class);
}
