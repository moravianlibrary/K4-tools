/**
 * Created by grman on 19.5.17.
 */
/*
finds desired information off dcouments which are loaded from a file containing uuids
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
def outputFile = new File("output_info.txt")
def volumeMissingFile = new File("volumeMissing.txt")
def doesNotHaveIssueFile = new File("doesNotHaveIssue.txt")
outputFile.write("") // some content may exist from previous run, erase the contents of file by writing ""
volumeMissingFile.write("")
doesNotHaveIssueFile.write("")

def sysno
def barCode
def ccnb
def urnnbn

def dsDisseminationXml = { String uuid, String dataStream ->
    def response = FedoraClient.getDatastreamDissemination(uuid, dataStream).execute().getEntity(String)
    return new XmlSlurper(false, true).parseText(response)
}

//file from which are the uuids loaded
File file = new File('/home/grman/Code/k4OLD/K4-tools/onlyNoAltoUuid.txt')
def lines = file.readLines()
int numOfLines = lines.size()-1

for (int i = 0; i< numOfLines; i++)
{
    lines[i] = lines[i].replace(" ", "")

//pick what information you need and change code to fit
    try {

        def modsNode = dsDisseminationXml(lines[i], "BIBLIO_MODS").mods
        def dcNode = dsDisseminationXml(lines[i], "DC")

        for (j = 0; j < modsNode.identifier."@type".size(); j++) {
            switch (modsNode.identifier."@type"[j]) {

                case "sysno": sysno = modsNode.identifier[j].text()
                    break
                case "barCode": barCode = modsNode.identifier[j].text()
                    break
                case "ccnb": ccnb = modsNode.identifier[j].text()
                    break
                case "urnnbn": urnnbn = modsNode.identifier[j].text()
            }
        }
        //outputFile << "${dcNode.titleInfo.title.toString()}; "
        outputFile << "${lines[i]};"
        outputFile << "${(((modsNode.titleInfo.title.toString()).replace("\n"," ")).replace("                "," ")).replace("  "," ")};"
        outputFile << "${modsNode.name.namePart[0].toString()};"
        outputFile << "${modsNode.originInfo.dateIssued.toString()};"
        outputFile << "${modsNode.location.shelfLocator};"
        outputFile << "$sysno;"
        outputFile << "$barCode;"
        outputFile << "${modsNode.location.physicalLocation.toString()}"

        outputFile << "http://kramerius.mzk.cz/search/handle/${lines[i]} $ln"
        urnnbn = null
        sysno = null
        barCode = null
        ccnb = null
    }
    catch (Exception e)
    {
        errFile<<"${lines[i]} $ln"
    }
    println(i)
}