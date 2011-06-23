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
 * See CyrillicCharset for supporting encodings.
 *
 * Algorithm looks how many invalid (i.e not not encountered in learning set)
 * triple combinations of russian chars and
 * how many invalid word thresholds (i.e. not alphabetic char + triple or triple + not alphabetic char).
 *
 * Optimized to detect encodings for short phrases.
 *
 * Non thread safe.
 */
public class CyrillicCharsetDetector {

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

    private final int rndInvalidCoef = 10;
    private final BitSet wordThresholdsTable;
    private final BitSet triplesTable;
    private final int[] charFrequenciesTable;

    public CyrillicCharsetDetector(BitSet wordThresholdsTable, BitSet triplesTable,
                                   int[] charFrequenciesTable) {
        super();
        this.wordThresholdsTable = wordThresholdsTable;
        this.triplesTable = triplesTable;
        this.charFrequenciesTable = charFrequenciesTable;
    }

    public static int getTripleIndex(CyrillicCharset cs, byte a, byte b, byte c) {
        final int indexA = cs.charToIndex(a);
        final int indexB = cs.charToIndex(b);
        final int indexC = cs.charToIndex(c);
        if (indexA < 0 || indexB < 0 || indexC < 0) {
            return -1;
        }
        return indexA * CHARS_NUM * CHARS_NUM + indexB * CHARS_NUM + indexC;
    }

    public static int getPairIndex(CyrillicCharset cs, byte a, byte b) {
        final int indexA = cs.charToIndex(a);
        final int indexB = cs.charToIndex(b);
        if (indexA < 0 || indexB < 0) {
            return -1;
        }
        return indexA * CHARS_NUM + indexB;
    }

    public static int getWordThresholdIndex(CyrillicCharset cs, byte a, byte b, byte c, byte d) {
        final int indexA = cs.charToIndex(a);
        final int indexB = cs.charToIndex(b);
        final int indexC = cs.charToIndex(c);
        final int indexD = cs.charToIndex(d);
        if (indexB < 0 || indexC < 0 || (indexA < 0 && indexD < 0)) {
            return -1;
        }
        if (indexA < 0) { // it is word's beginning 3 chars
            return indexB * CHARS_NUM * CHARS_NUM + indexC * CHARS_NUM + indexD;
        }
        if (indexD < 0) { // it is word's ending 3 chars
            return indexA * CHARS_NUM * CHARS_NUM + indexB * CHARS_NUM + indexC;
        }
        return -1; //it is  the middle of the word (4 valid chars)
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

        for (int i = 0; i < bytes.length - 3; i++) {
            for (CyrillicCharset cs : CyrillicCharset.values()) {
                 Stats stats = statsPerCharset.get(cs);

                 collectFrequenciesStats(stats, getPairIndex(cs, bytes[i], bytes[i + 1]));

                 int wordThresholdIndex =
                     getWordThresholdIndex(cs, bytes[i], bytes[i + 1], bytes[i + 2], bytes[i + 3]);

                 collectStats(wordThresholdsTable, stats, wordThresholdIndex);

                 int tripleIndex = getTripleIndex(cs, bytes[i], bytes[i + 1], bytes[i + 2]);
                 collectStats(triplesTable, stats, tripleIndex);
             }
         }

         return analyzeStats();
    }

    /**
     * Looks for charset with minimum number of invalid russian chars combinations
     * excepting those that have 0 russian chars combinations.
     *
     * If there are several ones then returns  charset
     * with maximum number of all russian chars combinations.
     *
     * @return detected charset
     */
    private CyrillicCharset analyzeStats() {
        CyrillicCharset best = CyrillicCharset.values()[0];

        for (CyrillicCharset cs : CyrillicCharset.values()) {
            Stats eachStats = statsPerCharset.get(cs);
            Stats bestStats = statsPerCharset.get(best);
            removeRandomIvalidCombinations(eachStats);
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

    private byte[] wrapWithSpaces(byte[] b) {
        byte[] bytes = new byte[b.length + 2];
        bytes[0] = SPACE_CHAR_CODE;
        for (int i = 0; i < b.length; i++) {
            bytes[i + 1] = b[i];
        }
        bytes[b.length + 1] = SPACE_CHAR_CODE;
        return bytes;
    }

    private void collectStats(BitSet table, Stats stats, int index) {
        if (index >= 0) {
            stats.all++;
            if (!table.get(index)) {
                stats.invalids++;
            }
        }
    }

    private void collectFrequenciesStats(Stats stats, int index) {
        if (index >= 0) {
            stats.frequencies *= charFrequenciesTable[index];
        }
    }

    private void removeRandomIvalidCombinations(Stats stats) {
            if (stats.invalids * rndInvalidCoef < stats.all) {
                stats.invalids = 0;
            }
    }

    private EnumMap<CyrillicCharset, Stats> createStats() {
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

    public void print() {
         for (CyrillicCharset cs : CyrillicCharset.values()) {
            Stats s = statsPerCharset.get(cs);
            System.out.println("CS: " + cs);
            System.out.println("All: " + s.all);
            System.out.println("Invalids: " + s.invalids);
            System.out.println("Freq: " + s.frequencies);
        }
    }
}
