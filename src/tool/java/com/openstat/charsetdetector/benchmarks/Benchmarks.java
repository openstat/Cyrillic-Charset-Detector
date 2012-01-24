package com.openstat.charsetdetector.benchmarks;

public final class Benchmarks {

    public static final String DELIMITER = ";";
    private long time;
    private int value;
    private int fails;
    private transient long startProfileTime;

    public Benchmarks() {
        reset();
    }

    public void reset() {
        time = 0;
        value = 0;
        fails = 0;
    }

    public void profileEnter() {
        startProfileTime = System.nanoTime();
    }

    public void profileExit() {
        time += System.nanoTime() - startProfileTime;
        value++;
    }

    public void failed() {
        fails++;
    }

    public long getAvarageTime() {
        return time / value;
    }

    public int getValue() {
        return value;
    }

    public int getSuccessPercents() {
        return (value - fails) * 100 / value;
    }

    public String toString() {
        return "" + getValue() + DELIMITER + getAvarageTime() + DELIMITER + getSuccessPercents();
    }

    public static String getHeaders() {
        return "Benchmarks' value" + DELIMITER + "Avarage time (ns)" + DELIMITER + "Successful attempts (%%)";
    }
}
