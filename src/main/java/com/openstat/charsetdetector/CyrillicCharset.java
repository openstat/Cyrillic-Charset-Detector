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

    WIN_1251 {
        @Override
        public Charset getNioCharset() {
            return NIO_CS_WIN_RU;
        }
    },
    KOI8_R {
        @Override
        public Charset getNioCharset() {
            return NIO_CS_KOI_RU;
        }
    },
    ISO_8859_5 {
        @Override
        public Charset getNioCharset() {
            return NIO_CS_ISO_RU;
        }
    },
    IBM855 {
        @Override
        public Charset getNioCharset() {
            return NIO_CS_IBM_RU;
        }
    };

    private static final Charset NIO_CS_IBM_RU = Charset.forName("IBM855");

    private static final Charset NIO_CS_ISO_RU = Charset.forName("ISO-8859-5");

    private static final Charset NIO_CS_KOI_RU = Charset.forName("KOI8-R");

    private static final Charset NIO_CS_WIN_RU = Charset.forName("windows-1251");

    public static final String ALL_CHARS = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюя";

    public static final int CHARS_NUM = 32;

    private int[] index = null;

    public abstract Charset getNioCharset();

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

    private int[] createIndex() {
        byte[] chars = ALL_CHARS.getBytes(getNioCharset());
        int[] ind = new int[256];
        for (int i = 0; i < 256; i++) {
            ind[i] = -1;
        }

        for (int i = 0; i < CHARS_NUM; i++) {
            ind[signToUnsign(chars[i])] = i;
            ind[signToUnsign(chars[i + CHARS_NUM])] = i;
        }

        byte[] yo = "ёЁ".getBytes(getNioCharset());
        ind[signToUnsign(yo[0])] = 5;
        ind[signToUnsign(yo[1])] = 5;

        return ind;
    }
}
