package com.openstat.charsetdetector;

import static com.openstat.charsetdetector.util.Util.signToUnsign;

import java.nio.charset.Charset;

public enum Language {

    RU("АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯIЇЄ'абвгдежзийклмнопрстуфхцчшщъыьэюяiїє'") {

        protected void memorizeAdditionalChars(Charset cs, int[] ind) {
            byte[] yo = "ёЁ".getBytes(cs);
            ind[signToUnsign(yo[0])] = 5;
            ind[signToUnsign(yo[1])] = 5;
        }
    };

    private final String characters;

    private final int charsNum;

    private Language(String characters) {
        this.characters = characters;
        this.charsNum = characters.length() / 2;
    }

    public String getCharacters() {
        return characters;
    }

    public int getCharsNum() {
        return charsNum;
    }

    public int[] createIndex(Charset cs) {
        byte[] charsBytes = characters.getBytes(cs);
        int[] index = new int[256];
        for (int i = 0; i < 256; i++) {
            index[i] = -1;
        }

        for (int i = 0; i < charsNum; i++) {
            index[signToUnsign(charsBytes[i])] = i;
            index[signToUnsign(charsBytes[i + charsNum])] = i;
        }

        memorizeAdditionalChars(cs, index);

        return index;
    }

    protected abstract void memorizeAdditionalChars(Charset cs, int[] ind);

}
