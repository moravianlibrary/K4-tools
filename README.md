K4-tools
========

řada (často nesouvisejících) skriptů k usnadnění správy K4

Při změně krameria ze starého na nový (vzniká chyba, že nemůže najít API-A nebo že API-A není korektní služba)
    1. Zakomentovat 2. řádek (<definitions) v souboru FedoraAPI-A.wsdl a odkomentovat ten pod ním. 
    2. V souboru AccessProvider je funkce s parametrem odkazujícím na: "Fedora-API-A-Service", tak zakomentovat a       odkomentovat zakomentovaný řádek pod ní.

Při přechodu zpět, prohodit zpátky.
