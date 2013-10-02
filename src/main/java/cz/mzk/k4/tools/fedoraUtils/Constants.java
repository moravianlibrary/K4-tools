/*
 * Metadata Editor
 * @author Jiri Kremser
 * 
 * 
 * 
 * Metadata Editor - Rich internet application for editing metadata.
 * Copyright (C) 2011  Jiri Kremser (kremser@mzk.cz)
 * Moravian Library in Brno
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * 
 */

package cz.mzk.k4.tools.fedoraUtils;

import com.google.gwt.event.shared.GwtEvent.Type;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class Constants.
 * 
 * @author Jiri Kremser
 */
public class Constants {

    /** The Constant FEDORA_INFO_PREFIX. */
    public static final String FEDORA_INFO_PREFIX = "info:fedora/";

    /**
     * delay after ingesting the object, fedora may fall down, if there is too
     * much requests.
     */
    public static final int REST_DELAY = 75;

    /**
     * Fedora FOXML related constants.
     */
    public static enum DATASTREAM_ID {

        /** The DC. */
        DC("DC"),
        /** The REL s_ ext. */
        RELS_EXT("RELS-EXT"),
        /** The BIBLI o_ mods. */
        BIBLIO_MODS("BIBLIO_MODS"),
        /** The POLICY. */
        POLICY("POLICY"),
        /** The IM g_ full. */
        IMG_FULL("IMG_FULL"),

        /** The IM g_ thumb. */
        IMG_THUMB("IMG_THUMB"),
        /** The IM g_ preview. */
        IMG_PREVIEW("IMG_PREVIEW"),
        /** The TEI. */
        TEI("TEI"),
        /** The TEX t_ ocr. */
        TEXT_OCR("TEXT_OCR"), WAV("WAV"), OGG("OGG"), MP3("MP3"),
        /** The ALTO. */
        ALTO("ALTO");

        /** The value. */
        private final String value;

        /**
         * Instantiates a new dATASTREA m_ id.
         *
         * @param value
         *        the value
         */
        private DATASTREAM_ID(String value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }


}
