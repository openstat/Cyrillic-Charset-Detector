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

import static com.openstat.charsetdetector.CyrillicCharset.CHARS_NUM;
import static com.openstat.charsetdetector.CyrillicCharsetDetector.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.BitSet;

public final class LearningCharsetDetector {

    private LearningCharsetDetector() {
    }

    public static void learnDetecting(String learnigSetPath, String outputDir, CyrillicCharset cs) {
        BitSet boundaryTrigrams = new BitSet(CHARS_NUM * CHARS_NUM * CHARS_NUM * 2);
        BitSet trigrams = new BitSet(CHARS_NUM * CHARS_NUM * CHARS_NUM);
        int[] digramsFrequencies = new int[CHARS_NUM * CHARS_NUM];
        Arrays.fill(digramsFrequencies, 1);
        File learningSet = new File(learnigSetPath);
        if (!learningSet.exists() || !learningSet.isFile()) {
            throw new RuntimeException(
                    "No data file to learn to detect russian encoding!" + learningSet.getAbsolutePath());
        }
        try {
            FileInputStream is = new FileInputStream(learningSet);
            int curChar = 0;
            int prevChar = 0;
            int prevPrevChar = 0;
            int prevPrevPrevChar = 0;
            while (is.available() > 0) {
                curChar = is.read();
                int ind1 = cs.charToIndex((byte) prevPrevPrevChar);
                int ind2 = cs.charToIndex((byte) prevPrevChar);
                int ind3 = cs.charToIndex((byte) prevChar);
                int ind4 = cs.charToIndex((byte) curChar);
                if (ind1 >= 0 && ind2 >= 0) {
                    digramsFrequencies[digramIndex(ind1, ind2)]++;
                }

                if (ind1 >= 0 && ind2 >= 0 && ind3 >= 0) {
                    trigrams.set(trigramIndex(ind1, ind2, ind3));
                }

                if (ind1 < 0 && ind2 >= 0 && ind3 >= 0 && ind4 >= 0) {
                    boundaryTrigrams.set(startBoundaryTrigramIndex(ind2, ind3, ind4));
                }

                if (ind1 >= 0 && ind2 >= 0 && ind3 >= 0 && ind4 < 0) {
                    boundaryTrigrams.set(endBoundaryTrigramIndex(ind1, ind2, ind3));
                }

                prevPrevPrevChar = prevPrevChar;
                prevPrevChar = prevChar;
                prevChar = curChar;
            }

            is.close();
            serializeStatsTables(boundaryTrigrams, trigrams, digramsFrequencies, outputDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void serializeStatsTables(BitSet boundaryTrigrams, BitSet trigrams,
            int[] frequencies, String outputDir)
            throws IOException {
        ObjectOutputStream boundaryTrigramsStream = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/wordThresholds.data"));
        boundaryTrigramsStream.writeObject(boundaryTrigrams);
        boundaryTrigramsStream.close();

        ObjectOutputStream trigramsStream = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/triples.data"));
        trigramsStream.writeObject(trigrams);
        trigramsStream.close();

        ObjectOutputStream frequenciesStream = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/frequencies.data"));
        frequenciesStream.writeObject(frequencies);
        frequenciesStream.close();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException(
                    "You must pass 3 arguments: path to learning set, output dir, learning set encoding.");
        }
        learnDetecting(args[0], args[1], CyrillicCharset.valueOf(args[2]));
        System.out.println("Learned!");
    }
}
