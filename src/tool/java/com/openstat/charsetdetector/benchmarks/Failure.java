package com.openstat.charsetdetector.benchmarks;

import static com.openstat.charsetdetector.benchmarks.Benchmarks.DELIMITER;

final class Failure implements Comparable<Failure> {
    private String expected;
    private String expCharset;
    private String actual;
    private String actCharset;

    public Failure(String expected, String expCharset, String actual, String actCharset) {
        super();
        this.expected = expected;
        this.expCharset = expCharset;
        this.actual = actual;
        this.actCharset = actCharset;
    }

    @Override
    public String toString() {
        return "" + expected + DELIMITER + expCharset + DELIMITER + actual + DELIMITER + actCharset;
    }

    public static String getHeaders() {
        return "Expected string" + DELIMITER + "Expected encoding"
            + DELIMITER + "Actual string" + DELIMITER + "Actual encoding";
    }

    @Override
    public int compareTo(Failure f2) {
        return expected.length() > f2.expected.length()
            ?
                1
                : expected.length() < f2.expected.length()
                    ?
                        -1
                        : expected.compareTo(f2.expected);
    }
}
