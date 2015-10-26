#!/bin/bash  
# usage find . -type f -exec /path/imageserverreplace.sh {}
TILES=`xmlstarlet sel -N kramerius4="http://www.nsdl.org/ontologies/relationships#" -t -v "//kramerius4:tiles-url" $1`
if [ ! -n "$TILES" ]; then
  echo $1";nopage"
else
  NEWTILES=${TILES/http:\/\/krameriusndktest.mzk.cz\/imageserver/http:\/\/imageserver.mzk.cz}
  THUMB=$NEWTILES/thumb.jpg
  PREVIEW=$NEWTILES/preview.jpg
  BIG=$NEWTILES/big.jpg
  echo $1";"$NEWTILES
  xmlstarlet ed --inplace  --pf  -N kramerius4="http://www.nsdl.org/ontologies/relationships#" \
   -N foxml="info:fedora/fedora-system:def/foxml#" \
   -u "//kramerius4:tiles-url" -v $NEWTILES \
   -u "//foxml:datastream[@ID='IMG_THUMB']//foxml:contentLocation/@REF" -v $THUMB \
   -u "//foxml:datastream[@ID='IMG_PREVIEW']//foxml:contentLocation/@REF" -v $PREVIEW \
   -u "//foxml:datastream[@ID='IMG_FULL']//foxml:contentLocation/@REF" -v $BIG \
  $1
fi


