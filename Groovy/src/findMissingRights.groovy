import groovy.xml.StreamingMarkupBuilder

/**
 * Created by mBurda on 11.10.16.
 */


@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.RiSearchResponse
import groovy.xml.XmlUtil
import groovy.xml.StreamingMarkupBuilder

//set variables needed for connection with server
def FEDORA_URL = System.getenv()['FEDORA_URL']
def FEDORA_USER = System.getenv()['FEDORA_USER']
def FEDORA_PASSWORD = System.getenv()['FEDORA_PASSWORD']

FedoraCredentials credentials = new FedoraCredentials(FEDORA_URL, FEDORA_USER, FEDORA_PASSWORD)
FedoraClient fedoraClient = new FedoraClient(credentials)
FedoraRequest.setDefaultClient(fedoraClient)

//napíš nejaký výpis, aby si vedel, že sa niečo deje

def getRiSearchResponse = { String query ->
    def RiSearch riSearch = new RiSearch(query)
    def RiSearchResponse riSearchResponse = riSearch.type('triples').lang('spo').format('N-Triples').distinct(true).template('')/*.limit(10)*/.execute()
    def response = riSearchResponse.getEntity(String.class)
    return response
    //return new XmlSlurper(false, false).parseText(response)
}


//def ln = System.getProperty('line.separator')
//def outputFile = new File("IO/missingDCrightsMonographs.txt")
def rightsFile = new File("IO/dcRights.txt")
def policyFile = new File("IO/policy.txt")
//outputFile.write("") // some content may exist from previous run, erase the contents of file by writing ""
rightsFile.write("")
policyFile.write("")
rightsFile.append(getRiSearchResponse("* <http://purl.org/dc/elements/1.1/rights> *").toString()
//        .replace(" <http://purl.org/dc/elements/1.1/rights>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
);

// najdeme všetky dokumenty a ich policy položky
policyFile.append(getRiSearchResponse("* <http://www.nsdl.org/ontologies/relationships#policy> *").toString()
//        .replace(" <http://www.nsdl.org/ontologies/relationships#policy>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
);


/*
/// najdeme všetky dokumenty a ich rights položky
rightsFile.append(getRiSearchResponse("<info:fedora/uuid:a101de00-2119-11e3-a5bb-005056827e52> <http://purl.org/dc/elements/1.1/rights> *").toString()
        .replace(" <http://purl.org/dc/elements/1.1/rights>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
        );

// najdeme všetky dokumenty a ich policy položky
policyFile.append(getRiSearchResponse("<info:fedora/uuid:70223083-fd5d-4313-8094-3dbfcd2e9e5d> <http://www.nsdl.org/ontologies/relationships#policy> *").toString()
        .replace(" <http://www.nsdl.org/ontologies/relationships#policy>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
        );

//policyFile a rightsFile majú formát "uuid:xxx policy:xxx"

//ak prepíšeme rightsFile na string, tak nemá zmysel aby sme ho predtým ukladali ako súbor
String rights = rightsFile.toString();

policyFile.eachLine {line ->
    if(!rights.contains(line)){
        outputFile.append(line.toString())
    } else {
        //zmenší velkosť rights, mohlo by zrýchliť hladanie
        rights.replace(line.toString(),"")
    }
}
*/

//----------------------------------------------------------------------------------------------


//bez dc:rights
// def monographsXML = getRiSearchResponse("<info:fedora/uuid:04d00e42-e38e-11e0-9907-001e4ff27ac1> * *")
//s dc:rights
//def monographsXML = getRiSearchResponse("<info:fedora/uuid:a101de00-2119-11e3-a5bb-005056827e52> <http://www.nsdl.org/ontologies/relationships#policy> *")


//def policFile = new File("IO/tmp.txt")
//policFile.write("")
//policFile.append(getRiSearchResponse("<info:fedora/uuid:04d00e42-e38e-11e0-9907-001e4ff27ac1> <http://www.nsdl.org/ontologies/relationships#policy> *").toString()
//        .replace(" <http://www.nsdl.org/ontologies/relationships#policy>","")/*.replace("<info:fedora/","").replace(">","").replace("\"","")*/.replace(" .","")
//        );


//String[] book = null
//String uuidPrev = ""
//def rightsPol;
//policFile.eachLine { line ->
//        book = line.split(" ")
//        if(book[0]==uuidPrev) return
//        rightsPol = getRiSearchResponse("${book[0]} <http://purl.org/dc/elements/1.1/rights> *").toString()
//                .replace(" <http://purl.org/dc/elements/1.1/rights>","")/*.replace("<info:fedora/","").replace(">","").replace("\"","")*/.replace(" .\n","").split(" ")
//        if(rightsPol.length >= 2) {
//            if (book[1] != (rightsPol[1])) outputFile.append("${book[0]} ${book[1]}\n")
//        } else outputFile.append("${book[0]} ${book[1]}\n")
//        uuidPrev = book[0]
//}

rightsFile.append(getRiSearchResponse("* <http://purl.org/dc/elements/1.1/rights> *").toString()
//        .replace(" <http://purl.org/dc/elements/1.1/rights>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
);

// najdeme všetky dokumenty a ich policy položky
policyFile.append(getRiSearchResponse("* <http://www.nsdl.org/ontologies/relationships#policy> *").toString()
//        .replace(" <http://www.nsdl.org/ontologies/relationships#policy>","").replace("<info:fedora/","").replace(">","").replace("\"","").replace(" .","")
);
