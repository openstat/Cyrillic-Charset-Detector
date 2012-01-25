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

import java.util.BitSet;
import java.util.EnumMap;

import static com.openstat.charsetdetector.CyrillicCharset.CHARS_NUM;

/**
 * Using for detection one byte cyrillic encoding.
 * See #CyrillicCharset for supporting encodings.
 *
 * Optimized to detect encodings for short byte sequences.
 *
 * Non thread safe.
 */
public final class CyrillicCharsetDetector {

    private static final int SPACE_CHAR_CODE = 0x20;
    private final EnumMap<CyrillicCharset, Stats> statsPerCharset = createStats();

    private static final class Stats {

        public int invalids = 0;
        public int all = 0;
        public long frequencies = 1;

        public void reset() {
            invalids = 0;
            all = 0;
            frequencies = 1;
        }
    }
    /**
     * Digram is a sequence of 2 cyrillic chars.
     * This table presents how many times digram with number 'i' 
     * meets in the learning-set text.
     */
    private final int[] charFrequenciesTable;
    /**
     * Trigram is a sequence of 3 cyrillic chars.
     * This table presents if trigram with number 'i' exists in the learning-set text.
     * These frequencies are incremented by 1 (the minimum value of this table is 1)
     */
    private final BitSet trigramsTable;
    /**
     * Boundary trigram is the first or the last trigram in a cyrillic word.
     * This table presents if trigram with number 'i' exists in learning-set text.
     */
    private final BitSet boundaryTrigramsTable;

    public CyrillicCharsetDetector(BitSet wordThresholdsTable, BitSet triplesTable,
            int[] charFrequenciesTable) {
        super();
        this.boundaryTrigramsTable = wordThresholdsTable;
        this.trigramsTable = triplesTable;
        this.charFrequenciesTable = charFrequenciesTable;
    }

    /**
     * Detects which one-byte cyrillic charset was used to encode the string into input bytes' array.
     *
     * @param bytes
     * @return CyrillicCharset instance
     */
    public CyrillicCharset detectCyrillicCharset(byte[] b) {
        if (b.length < 3) { // too small to analyze
            return CyrillicCharset.values()[0]; // most popular
        }

        resetStats();

        byte[] bytes = wrapWithSpaces(b);
        for (CyrillicCharset cs : CyrillicCharset.values()) {
            Stats stats = statsPerCharset.get(cs);
            // We go through all available 4-chars sequences 
            // and look if they matches the patterns of digram,
            // trigram or boundary trigram.
            // 'A' = any cyrillic char (one or #CyrillicCharset.CHARS)
            // '^' = other chars (spaces, punctuation marks, numbers, latin chars etc)
            // '*' = any char
            int index1;
            // Char index is its alfabetic number if it is a cyrillic char
            // or -1 otherwise. 
            int index2 = cs.charToIndex(bytes[0]);
            int index3 = cs.charToIndex(bytes[1]);
            int index4 = cs.charToIndex(bytes[2]);
            for (int i = 0; i < bytes.length - 3; i++) {
                index1 = index2;
                index2 = index3;
                index3 = index4;
                index4 = cs.charToIndex(bytes[i + 3]);
                // chars sequence pattern: AA**
                if (index1 >= 0 && index2 >= 0) {
                    collectFrequenciesStats(stats, digramIndex(index1, index2));
                    // chars sequence pattern: AAA*
                    if (index3 >= 0) {
                        collectStats(trigramsTable, stats,
                                trigramIndex(index1, index2, index3));
                        // chars sequence pattern: AAA^
                        if (index4 < 0) {
                            collectStats(boundaryTrigramsTable, stats,
                                    endBoundaryTrigramIndex(index1, index2, index3));
                        }

                    }
                    // chars sequence pattern: ^AAA
                } else if (index1 < 0 && index2 >= 0 && index2 >= 0 && index3 >= 0 && index4 >= 0) {
                    collectStats(boundaryTrigramsTable, stats,
                            startBoundaryTrigramIndex(index2, index3, index4));
                }
            }
        }

        return analyzeStats();
    }

    public static int startBoundaryTrigramIndex(int index2, int index3, int index4) {
        int boundaryTrigramIndex = CHARS_NUM * CHARS_NUM * CHARS_NUM + index2 * CHARS_NUM * CHARS_NUM + index3 * CHARS_NUM + index4;
        return boundaryTrigramIndex;
    }

    public static int endBoundaryTrigramIndex(int index1, int index2, int index3) {
        int boundaryTrigramIndex = index1 * CHARS_NUM * CHARS_NUM + index2 * CHARS_NUM + index3;
        return boundaryTrigramIndex;
    }

    public static int digramIndex(int index1, int index2) {
        int digramIndex = index1 * CHARS_NUM + index2;
        return digramIndex;
    }

    public static int trigramIndex(int index1, int index2, int index3) {
        int trigramIndex = index1 * CHARS_NUM * CHARS_NUM + index2 * CHARS_NUM + index3;
        return trigramIndex;
    }

    /**
     * Choosing the best charset encoding.
     * First we choose decoded string with the maximum of cyrillic trigrams
     * (the others most likely has a garbage).
     * Then we choose strings with the minimum invalid trigrams 
     * (the trigrams that doesn't exist in learning-set).
     * Finally if we have more then one string we choose the one with the 
     * maximum digramfs frequencies coefficient. 
     * cyrillic trigrams (It means that)
     * @return detected charset
     */
    private CyrillicCharset analyzeStats() {
        CyrillicCharset best = CyrillicCharset.values()[0];

        for (CyrillicCharset cs : CyrillicCharset.values()) {
            Stats eachStats = statsPerCharset.get(cs);
            Stats bestStats = statsPerCharset.get(best);
            if (eachStats.all > bestStats.all
                    || (eachStats.all == bestStats.all
                    && (eachStats.invalids < bestStats.invalids
                    || (eachStats.invalids == bestStats.invalids
                    && eachStats.frequencies > bestStats.frequencies)))) {

                best = cs;
            }
        }
        return best;
    }

    private static byte[] wrapWithSpaces(byte[] b) {
        byte[] bytes = new byte[b.length + 2];
        bytes[0] = SPACE_CHAR_CODE;
        System.arraycopy(b, 0, bytes, 1, b.length);
        bytes[b.length + 1] = SPACE_CHAR_CODE;
        return bytes;
    }

    // assert index > 0
    private void collectStats(BitSet table, Stats stats, int index) {
        stats.all++;
        if (!table.get(index)) {
            stats.invalids++;
        }
    }

    // assert index > 0
    private void collectFrequenciesStats(Stats stats, int index) {
        stats.frequencies *= charFrequenciesTable[index];
    }

    private static EnumMap<CyrillicCharset, Stats> createStats() {
        EnumMap<CyrillicCharset, Stats> newStats = new EnumMap<CyrillicCharset, Stats>(CyrillicCharset.class);
        for (CyrillicCharset cs : CyrillicCharset.values()) {
            newStats.put(cs, new Stats());
        }
        return newStats;
    }

    private void resetStats() {
        for (CyrillicCharset cs : CyrillicCharset.values()) {
            statsPerCharset.get(cs).reset();
        }
    }
}
