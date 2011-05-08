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
import static com.openstat.charsetdetector.CyrillicCharsetDetector.getTripleIndex;
import static com.openstat.charsetdetector.CyrillicCharsetDetector.getWordThresholdIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.BitSet;

public final class LearningCharsetDetector {

    private LearningCharsetDetector() {}

    public static void learnDetecting(String learnigSetPath, String outputDir, CyrillicCharset cs) {
        BitSet wordThresholds = new BitSet(CHARS_NUM * CHARS_NUM * CHARS_NUM * 2);
        BitSet triples = new BitSet(CHARS_NUM * CHARS_NUM * CHARS_NUM);
        File learningSet = new File(learnigSetPath);
        if (!learningSet.exists() || !learningSet.isFile()) {
            throw new RuntimeException(
                    "No data file to learn to detect russian encoding!" + learningSet.getAbsolutePath());
        }
        try {
            FileInputStream is = new FileInputStream(learningSet);
            int nextChar = 0;
            int prevChar = 0;
            int prevPrevChar = 0;
            int prevPrevPrevChar = 0;
            while (is.available() > 0) {
                nextChar = is.read();

                final int tripleIndex = getTripleIndex(cs, (byte) prevPrevChar, (byte) prevChar, (byte) nextChar);
                if (prevChar != 0 && prevPrevChar != 0
                        && tripleIndex >= 0) {
                   triples.set(tripleIndex);
                }

                final int wordThresholdIndex = getWordThresholdIndex(cs, (byte) prevPrevPrevChar,
                        (byte) prevPrevChar, (byte) prevChar, (byte) nextChar);
                if (prevChar != 0 && prevPrevChar != 0  && prevPrevPrevChar != 0
                        && wordThresholdIndex >= 0) {
                    wordThresholds.set(wordThresholdIndex);
                }

                prevPrevPrevChar = prevPrevChar;
                prevPrevChar = prevChar;
                prevChar = nextChar;
            }

            is.close();
            serializeStatsTables(wordThresholds, triples, outputDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void serializeStatsTables(BitSet wordThresholds, BitSet triples, String outputDir)
            throws IOException {
        ObjectOutputStream wordThresholdsStream = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/wordThresholds.data"));
        wordThresholdsStream.writeObject(wordThresholds);
        wordThresholdsStream.close();

        ObjectOutputStream triplesStream = new ObjectOutputStream(
                new FileOutputStream(outputDir + "/triples.data"));
        triplesStream.writeObject(triples);
        triplesStream.close();
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
