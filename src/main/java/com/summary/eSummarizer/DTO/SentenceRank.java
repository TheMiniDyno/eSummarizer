package com.summary.eSummarizer.DTO;

public class SentenceRank {
    private String sentence;
    private double rank;

    public SentenceRank(String sentence, double rank) {
        this.sentence = sentence;
        this.rank = rank;
    }

    public String getSentence() {
        return sentence;
    }

    public double getRank() {
        return rank;
    }
}
