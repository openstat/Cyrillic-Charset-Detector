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

import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.BitSet;
/**
 * Detects a cyrillic charset by array of bytes.
 * Supported charsets: utf-8, win-1251, koi8-r, iso-8859-5, ibm855.
 * Optimized to detect charsets for short phrases.
 * Non thread safe.
 *
 */
public final class CharsetDetector {

    private static final Charset NIO_CS_UTF = Charset.forName("UTF-8");

    private CyrillicCharsetDetector cyrDetector = getCyrDetector();

    /**
     * Factory method for CyrillicCharsetDetector.
     * @return CyrillicCharsetDetector instance
     */
    public static CyrillicCharsetDetector getCyrDetector() {
            try {
                ObjectInputStream wordThresholdsStream = new ObjectInputStream(
                   CharsetDetector.class.getResourceAsStream("/wordThresholds.data"));
                BitSet wordThresholds = (BitSet) wordThresholdsStream.readObject();
                wordThresholdsStream.close();

                ObjectInputStream triplesStream = new ObjectInputStream(
                    CharsetDetector.class.getResourceAsStream("/triples.data"));
                BitSet triples  = (BitSet) triplesStream.readObject();
                triplesStream.close();

                ObjectInputStream frequenciesStream = new ObjectInputStream(
                    CharsetDetector.class.getResourceAsStream("/frequencies.data"));
                int[] frequencies  = (int[]) frequenciesStream.readObject();
                frequenciesStream.close();

                return new CyrillicCharsetDetector(wordThresholds, triples, frequencies);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    /**
     * Detects a cyrillic charset by array of bytes.
     *
     * @param b bytes' array
     * @return java.nio.charset.Charset instance
     */
    public Charset detectNioCharset(byte[] b) {
        if (UTF8CharsetDetector.isUTF8(b)) {
            return NIO_CS_UTF;
        }
        return cyrDetector.detectCyrillicCharset(b).getNioCharset();
    }
}
