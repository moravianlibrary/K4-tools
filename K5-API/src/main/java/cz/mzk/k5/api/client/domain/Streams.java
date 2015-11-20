package cz.mzk.k5.api.client.domain;

/**
 * Created by holmanj on 19.11.15.
 */
public class Streams {

    // po načtení je u neexistujících datastreamů NULL
    StreamInfo DC;
    StreamInfo BIBLIO_MODS;
    StreamInfo TEXT_OCR;
    StreamInfo ALTO;
    StreamInfo IMG_FULL;
    StreamInfo IMG_PREVIEW;
    StreamInfo IMG_THUMB;
    StreamInfo IMG_FULL_ADM;
    StreamInfo TEXT_OCR_ADM;
    StreamInfo WAV;
    StreamInfo MP3;
    StreamInfo OGG;

    public StreamInfo getDC() {
        return DC;
    }

    public void setDC(StreamInfo DC) {
        this.DC = DC;
    }

    public StreamInfo getBIBLIO_MODS() {
        return BIBLIO_MODS;
    }

    public void setBIBLIO_MODS(StreamInfo BIBLIO_MODS) {
        this.BIBLIO_MODS = BIBLIO_MODS;
    }

    public StreamInfo getTEXT_OCR() {
        return TEXT_OCR;
    }

    public void setTEXT_OCR(StreamInfo TEXT_OCR) {
        this.TEXT_OCR = TEXT_OCR;
    }

    public StreamInfo getALTO() {
        return ALTO;
    }

    public void setALTO(StreamInfo ALTO) {
        this.ALTO = ALTO;
    }

    public StreamInfo getIMG_FULL() {
        return IMG_FULL;
    }

    public void setIMG_FULL(StreamInfo IMG_FULL) {
        this.IMG_FULL = IMG_FULL;
    }

    public StreamInfo getIMG_PREVIEW() {
        return IMG_PREVIEW;
    }

    public void setIMG_PREVIEW(StreamInfo IMG_PREVIEW) {
        this.IMG_PREVIEW = IMG_PREVIEW;
    }

    public StreamInfo getIMG_THUMB() {
        return IMG_THUMB;
    }

    public void setIMG_THUMB(StreamInfo IMG_THUMB) {
        this.IMG_THUMB = IMG_THUMB;
    }

    public StreamInfo getIMG_FULL_ADM() {
        return IMG_FULL_ADM;
    }

    public void setIMG_FULL_ADM(StreamInfo IMG_FULL_ADM) {
        this.IMG_FULL_ADM = IMG_FULL_ADM;
    }

    public StreamInfo getTEXT_OCR_ADM() {
        return TEXT_OCR_ADM;
    }

    public void setTEXT_OCR_ADM(StreamInfo TEXT_OCR_ADM) {
        this.TEXT_OCR_ADM = TEXT_OCR_ADM;
    }

    public StreamInfo getWAV() {
        return WAV;
    }

    public void setWAV(StreamInfo WAV) {
        this.WAV = WAV;
    }

    public StreamInfo getMP3() {
        return MP3;
    }

    public void setMP3(StreamInfo MP3) {
        this.MP3 = MP3;
    }

    public StreamInfo getOGG() {
        return OGG;
    }

    public void setOGG(StreamInfo OGG) {
        this.OGG = OGG;
    }

    @Override
    public String toString() {
        return "Streams{" +
                "DC=" + DC +
                ", BIBLIO_MODS=" + BIBLIO_MODS +
                ", TEXT_OCR=" + TEXT_OCR +
                ", ALTO=" + ALTO +
                ", IMG_FULL=" + IMG_FULL +
                ", IMG_PREVIEW=" + IMG_PREVIEW +
                ", IMG_THUMB=" + IMG_THUMB +
                ", IMG_FULL_ADM=" + IMG_FULL_ADM +
                ", TEXT_OCR_ADM=" + TEXT_OCR_ADM +
                ", WAV=" + WAV +
                ", MP3=" + MP3 +
                ", OGG=" + OGG +
                '}';
    }
}
