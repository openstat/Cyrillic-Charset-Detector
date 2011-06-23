package com.openstat.charsetdetector.benchmarks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.openstat.charsetdetector.util.Util.createWriter;
import java.util.Collections;
import java.util.TreeMap;
import java.util.regex.Pattern;
import static com.openstat.charsetdetector.benchmarks.Benchmarks.DELIMITER;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

import com.openstat.charsetdetector.CharsetDetector;

@Test(groups = "benchmarks")
public class BenchmarksTests {

    private static final AssertStrategy NORMAL_ASSERTION = new AssertStrategy() {
                @Override
                public void checkResults(int tries, int failures) {
                    assertTrue(tries / 66 > failures);
                }
            };
    private static final Pattern CLEANUP = Pattern.compile("[^a-zA-Z1-9а-яА-ЯїЇiIЄє'\\s\\-]");
    private static final Pattern NOT_GARBAGE = Pattern.compile("[а-яА-ЯїЇiIЄє]{3,}");


    private static final Charset[] RU_CHARSETS = new Charset[] {
        Charset.forName("UTF-8"),
        Charset.forName("Windows-1251"),
        Charset.forName("KOI8-R")
    };

    private static final Charset[] UA_CHARSETS = new Charset[] {
        Charset.forName("UTF-8"),
        Charset.forName("Windows-1251"),
        Charset.forName("KOI8-U")
    };

    private static final int INIT_MAX_TIME = 50000000;

    private static final Charset NIO_CS_WIN_1251 = Charset.forName("windows-1251");

    private static final int INIT_VALUE = 1000;

    private String testOutput = ".";
    private int segmentSize = 2;
    private int wordsInPhrase = 3;

    @Parameters("test.output")
    @BeforeTest
    public void setTestOutput(String testOutput) {
        this.testOutput = testOutput;
    }

    @Parameters("test.segment.size")
    @BeforeTest
    public void setSegmentSize(String segmentSize) {
        this.segmentSize = Integer.parseInt(segmentSize);
    }

    @Parameters("test.words.num")
    @BeforeTest
    public void setWordsInPhrase(String wordsInPhrase) {
        this.wordsInPhrase = Integer.parseInt(wordsInPhrase);
    }

    @Test
    public void testInit() throws IOException {
        Benchmarks benchmarks = new Benchmarks();
        for (int i = 0; i < INIT_VALUE; i++) {
            benchmarks.profileEnter();
            CharsetDetector detector = new CharsetDetector();
            detector.detectNioCharset("слово".getBytes(NIO_CS_WIN_1251));
            benchmarks.profileExit();
        }
        dumpInitBenchmarks(benchmarks);
        assertTrue(benchmarks.getAvarageTime() < INIT_MAX_TIME);
    }

    @Test
    public void testDetectStrugatskie() throws IOException {
        testDetect("veschi-veka.txt", NORMAL_ASSERTION, RU_CHARSETS);
    }

    @Test
    public void testDetectPolitology() throws IOException {
        testDetect("politology.txt", NORMAL_ASSERTION, RU_CHARSETS);
    }

    @Test
    public void testDetectUkrain() throws IOException {
        testDetect("ukr.txt", NORMAL_ASSERTION, UA_CHARSETS);
    }

    @Test
    public void testGarbage() throws IOException {
        testDetect("garbage.txt", new AssertStrategy() {
            @Override
            public void checkResults(int tries, int failures) {
                assertTrue(tries / 3 < failures);
            }
        }, RU_CHARSETS);
    }

    private void testDetect(String textFile, AssertStrategy assertion, Charset[] charsetsToTest) throws IOException {
        List<Failure> failures = new ArrayList<Failure>();
        Map<Integer, Benchmarks> benchmarksResults = new TreeMap<Integer, Benchmarks>();
        CharsetDetector detector = new CharsetDetector();
        ArrayList<String> words = splitBenchmarksTextIntoWords(textFile);
        int i = 0;
        int phraseLength = 1;
        int triesNum = 0;

        while (i + phraseLength <= words.size()) {
            StringBuilder phraseToTest = new StringBuilder();
            for (int j = 0; j < phraseLength; j++) {
                 phraseToTest.append(' ');
                 phraseToTest.append(words.get(i));
                 i++;
            }
            String preparedPhrase = phraseToTest.toString().trim();
            if (NOT_GARBAGE.matcher(preparedPhrase).find()) {
                triesNum++;
                runBenchmarksOnPhrase(charsetsToTest, detector, preparedPhrase, benchmarksResults, failures);
            }
            phraseLength = (phraseLength % wordsInPhrase) + 1;
        }

        triesNum *= charsetsToTest.length;
        Collections.sort(failures);

        dumpBenchmarksResults(textFile, benchmarksResults, triesNum);
        dumpFailures(textFile, failures, triesNum);

        assertion.checkResults(triesNum, failures.size());
    }

    private void runBenchmarksOnPhrase(Charset[] charsetsToTest, CharsetDetector detector, String phrase,
                Map<Integer, Benchmarks> benchmarksResults, List<Failure> failures) {
        for (Charset cs : charsetsToTest) {
            byte[] bytes = phrase.getBytes(cs);
            Benchmarks benchmarks = getBenchmarksByPhraseLength(phrase.length(), benchmarksResults);
            benchmarks.profileEnter();
            Charset detected = detector.detectNioCharset(bytes);
            benchmarks.profileExit();

            String decodedString = new String(bytes, detected);
            if (!phrase.equals(decodedString)) {

                benchmarks.failed();
                failures.add(new Failure(phrase, cs.toString(), decodedString, detected.toString()));
            }
        }
    }

    private Benchmarks getBenchmarksByPhraseLength(int phraseLength,
            Map<Integer, Benchmarks> benchmarksResults) {
        int segment = phraseLength - (phraseLength % segmentSize) + segmentSize;
        Benchmarks benchmarks;
        if (benchmarksResults.containsKey(segment)) {
            benchmarks = benchmarksResults.get(segment);
        } else {
            benchmarks = new Benchmarks();
            benchmarksResults.put(segment, benchmarks);
        }
        return benchmarks;
    }

    private ArrayList<String> splitBenchmarksTextIntoWords(String textFile) throws IOException {
        BufferedReader writer = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/benchmarks/" + textFile), "UTF-8"));
        ArrayList<String> words = new ArrayList<String>();
        String line;
        Pattern split = Pattern.compile(" ");
        while ((line = writer.readLine()) != null) {
            String[] wordsPerLine = split.split(line);
            for (String word : wordsPerLine) {
                String preparedWord = prepareWord(word);

                if (preparedWord.length() >= 1) {
                    words.add(preparedWord);
                }
            }
        }
        writer.close();
        return words;
    }

    private String prepareWord(String word) {
        return CLEANUP.matcher(word).replaceAll("");
    }

    private void dumpInitBenchmarks(Benchmarks benchmarks) throws IOException {
        BufferedWriter writer = createWriter(testOutput + "/init.csv");
        writer.write(Benchmarks.getHeaders());
        writer.newLine();
        writer.write(benchmarks.toString());
        writer.newLine();
        writer.close();
    }

    private void dumpFailures(String textFile, List<Failure> failures, int triesNum) throws IOException {
        BufferedWriter writer = createWriter(testOutput + "/" + textFile + ".failures.csv");
        writer.write("Number of tries:" + DELIMITER + triesNum);
        writer.newLine();
        writer.write("Number of failures:" + DELIMITER + failures.size());
        writer.newLine();
        writer.newLine();
        writer.write(Failure.getHeaders());
        writer.newLine();
        for (Failure failure : failures) {
            writer.write(failure.toString());
            writer.newLine();
        }
        writer.close();
    }

    private void dumpBenchmarksResults(String textFile, Map<Integer, Benchmarks> benchmarksResults, int triesNum)
                throws IOException {
        BufferedWriter writer = createWriter(testOutput + "/" + textFile + ".detect.csv");
        writer.write("Number of tries:" + DELIMITER + triesNum);
        writer.newLine();
        writer.newLine();
        writer.write("Length of phrase" + DELIMITER + Benchmarks.getHeaders());
        writer.newLine();
        for (int length : benchmarksResults.keySet()) {
            writer.write(""  + length + DELIMITER + benchmarksResults.get(length).toString());
            writer.newLine();
        }
        writer.close();
    }

    private static interface AssertStrategy {
        void checkResults(int tries, int failures);
    }
}

