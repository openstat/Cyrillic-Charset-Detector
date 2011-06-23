/**
 *    Copyright (c) 2011 Openstat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.openstat.charsetdetector;
import static com.openstat.charsetdetector.util.Util.signToUnsign;

import java.nio.charset.Charset;

public enum CyrillicCharset {

    WIN_1251("windows-1251"),
    KOI8_R("KOI8-R"),
    KOI8_U("KOI8-U"),
    CP866("Cp866");

    private static final String CHARS = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯIЇЄ'абвгдежзийклмнопрстуфхцчшщъыьэюяiїє'";
    public static final int CHARS_NUM = CHARS.length() / 2;
    private int[] index = null;

    private final Charset nioCharset;


    private CyrillicCharset(String nioCharsetName) {
        this.nioCharset = Charset.forName(nioCharsetName);
    }

    public Charset getNioCharset() {
        return nioCharset;
    }

    /**
     * If decoded character is a letter then index is number of that letter in alfabet.
     * Otherwise index is -1
     *
     * @param b byte of character
     * @return index
     */
    public int charToIndex(byte b) {
        if (index == null) {
            index = createIndex();
        }

        return index[signToUnsign(b)];
    }

    private void memorizeAdditionalChars(Charset cs, int[] ind) {
        byte[] yo = "ёЁ".getBytes(cs);
        ind[signToUnsign(yo[0])] = 5;
        ind[signToUnsign(yo[1])] = 5;
    }

    public int[] createIndex() {
        byte[] charsBytes = CHARS.getBytes(nioCharset);
        int[] ind = new int[256];
        for (int i = 0; i < 256; i++) {
            ind[i] = -1;
        }

        for (int i = 0; i < CHARS_NUM; i++) {
            ind[signToUnsign(charsBytes[i])] = i;
            ind[signToUnsign(charsBytes[i + CHARS_NUM])] = i;
        }

        memorizeAdditionalChars(nioCharset, ind);

        return ind;
    }


}
