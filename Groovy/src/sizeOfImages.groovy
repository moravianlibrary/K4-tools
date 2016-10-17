/**
 * Created by grman on 11.10.16.
 */
/*
Script funguje zatial iba na periodikach, a je najskor potrebne si pripojit directory pomocou sshfs v ktorom sa obrazki nachadzaju.
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
def UUID = 'uuid:1c869c00-535b-11e3-9ea2-5ef3fc9ae867' //uuid periodika
int size = 0;
def ln = System.getProperty('line.separator')
def errorFile = new File("error.txt")
errorFile.write("")

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD)
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)

def getRiSearchResponse = { String query ->
    def RiSearch riSearch = new RiSearch(query)
    def RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('RDF/XML').distinct(true).template('').execute()
    def response = riSearchResponse.getEntity(String.class)
    return new XmlSlurper(false, true).parseText(response)
}

def relsExtXml = getRiSearchResponse("<info:fedora/$UUID> <http://www.nsdl.org/ontologies/relationships#hasVolume> *")
print ("pocet rocnikov = ${relsExtXml.Description.hasVolume.size()} $ln")
for (int i = 0; i < relsExtXml.Description.hasVolume.size(); i++){
    print ("$ln $i. ")
    def volumeUuid = relsExtXml.Description.hasVolume[i].'@rdf:resource'.toString()
    volumeUuid = volumeUuid.replace("info:fedora/", "")
    def itemsXml = getRiSearchResponse("<info:fedora/$volumeUuid> <http://www.nsdl.org/ontologies/relationships#hasItem> *")
    for (int j = 0; j < itemsXml.Description.hasItem.size(); j++){
        def itemUuid = itemsXml.Description.hasItem[j].'@rdf:resource'.toString()
        itemUuid = itemUuid.replace("info:fedora/", "")
        def pagesXml = getRiSearchResponse("<info:fedora/$itemUuid> <http://www.nsdl.org/ontologies/relationships#hasPage> *")
        def pageUuid = pagesXml.Description.hasPage[0].'@rdf:resource'.toString()
        pageUuid = pageUuid.replace("info:fedora/", "")

        def imageDataStreamProfile = null
        try {
            imageDataStreamProfile = FedoraClient.getDatastream(pageUuid, "IMG_FULL").execute().getDatastreamProfile()
        } catch (Exception e) {
            errorFile << "Something is wrong with pages of document: $UUID"
        }
        def imagePath = imageDataStreamProfile.getDsLocation()
        def splitPath = imagePath.split("/")
        path = splitPath[3] + "/" + splitPath[4] + "/" + splitPath [5] + "/" + splitPath[6]
        def imagesDir
        try {
            imagesDir = new File("/home/grman/obrazky/$path")  //sem pripojit directory s obrazkami
        }
        catch (Exception e)
        {
            errorFile << "something is wrong with the path"
        }
        size = size + imagesDir.directorySize()/1024/1024
        print ("$size ")
    }
}

print ("     TOTAL SIZE = $size")

// TODO: monografie