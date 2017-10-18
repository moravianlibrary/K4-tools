
/**
 * Created by grman on 16.10.17.
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
def outputFile = new File("missingAlto.txt")
outputFile.write("") // some content may exist from previous run, erase the contents of file by writing ""

def getRiSearchResponse = { String query ->
    def RiSearch riSearch = new RiSearch(query)
    def RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('RDF/XML').distinct(true).template('').execute()
    def response = riSearchResponse.getEntity(String.class)
    return new XmlSlurper(false, true).parseText(response)
}

File file = new File('cesta k suboru s uuid')
def lines = file.readLines()
int numOfLines = lines.size()

for (int i = 0; i< numOfLines; i++) {
    lines[i] = lines[i].replace(" ", "")
    def pagesXml = getRiSearchResponse("<info:fedora/${lines[i]}> <http://www.nsdl.org/ontologies/relationships#hasPage> *")
    for (int j = 0; j< pagesXml.Description.hasPage.'@rdf:resource'.size(); j++) {
        def pageUuid = pagesXml.Description.hasPage[j].'@rdf:resource'.toString()
        pageUuid = pageUuid.replace("info:fedora/", "")
        try {
            def ocrStream = FedoraClient.getDatastream(pageUuid, "ALTO").execute().getDatastreamProfile()
        } catch (Exception e) {
            outputFile << "${pageUuid} $ln"
        }
    }
}