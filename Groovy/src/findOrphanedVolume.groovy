#!/usr/bin/env groovy
@Grab(group = 'com.sun.jersey', module = 'jersey-core', version = '1.17.1')
@Grab('com.yourmediashelf.fedora.client:fedora-client-core:0.7')
@GrabExclude('xml-apis:xml-apis')
@GrabExclude('xerces:xercesImpl')
import com.yourmediashelf.fedora.client.*
import com.yourmediashelf.fedora.client.request.*
import com.yourmediashelf.fedora.client.response.*;


FedoraCredentials credentials = new FedoraCredentials('http://fedoratest.mzk.cz/fedora', "fedoraAdmin", "fedoraAdmin");
FedoraClient fedoraClient = new FedoraClient(credentials);
FedoraRequest.setDefaultClient(fedoraClient);

// risearch dotaz na fedory na vsechny model periodicalvolume
RiSearch riSearch = new RiSearch('* <info:fedora/fedora-system:def/model#hasModel> <info:fedora/model:periodicalvolume>')
riSearch.type('triples')
riSearch.lang('spo')
riSearch.format('N-Triples')
riSearch.limit(10000)
riSearch.distinct(true)
riSearch.template('')
RiSearchResponse riSearchResponse = riSearch.execute()
def volumes = riSearchResponse.getEntity(String.class)

// odpoved z risearch zpracovame po radcich
volumes.eachLine { line ->
    // z jednotlivych radku parsujeme uuid
    def uuid = line.find("uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    // risearch dotaz do fedory, ktery vrati vsechny rodice daneho peridocialvolume
    RiSearch riParent = new RiSearch('* <http://www.nsdl.org/ontologies/relationships#hasVolume> <info:fedora/' + uuid + '>')
    riParent.type('triples')
    riParent.lang('spo')
    riParent.format('N-Triples')
    riParent.limit(10)
    riParent.distinct(true)
    riParent.template('')
    RiSearchResponse parentResponse = riParent.execute()
    // odpoved z risearch si prevedeme do listu
    def parents = parentResponse.getEntity(String.class).readLines()

    // list obsahuje nula rodicu, volume je tedy sirotek
    if (parents.size() == 0) {
        println("volume " + uuid + " je sirotek")
    } else if (parents.size() > 1) {
        println("volume " + uuid + " je pripojeno do vice objektu")
    }
//  pokud chces vypisovat i volumy, ktere jsou v poradku odkomentuj nasledujici radky
//    else {
//        println("volume " + uuid + " je v poradku")
//    }
}
