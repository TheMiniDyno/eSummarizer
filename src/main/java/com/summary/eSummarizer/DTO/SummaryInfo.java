package com.summary.eSummarizer.DTO;

import java.util.List;

public class SummaryInfo {
    private String summarizedText;
    private int originalSentenceCount;
    private int summarizedSentenceCount;
    private int originalWordCount;
    private int summarizedWordCount;
    private double reductionRate;
    private List<SentenceRank> sentenceRanks;
    private List<GraphNode> graphNodes;
    private List<GraphLink> graphLinks;

    public SummaryInfo(String summarizedText, int originalSentenceCount, int summarizedSentenceCount,
                       int originalWordCount, int summarizedWordCount, double reductionRate) {
        this.summarizedText = summarizedText;
        this.originalSentenceCount = originalSentenceCount;
        this.summarizedSentenceCount = summarizedSentenceCount;
        this.originalWordCount = originalWordCount;
        this.summarizedWordCount = summarizedWordCount;
        this.reductionRate = reductionRate;
    }

    // Getters and Setters
    public void setSentenceRanks(List<SentenceRank> sentenceRanks) {
        this.sentenceRanks = sentenceRanks;
    }

    public List<SentenceRank> getSentenceRanks() {
        return sentenceRanks;
    }

    public String getSummarizedText() {
        return summarizedText;
    }

    public int getOriginalSentenceCount() {
        return originalSentenceCount;
    }

    public int getSummarizedSentenceCount() {
        return summarizedSentenceCount;
    }

    public int getOriginalWordCount() {
        return originalWordCount;
    }

    public int getSummarizedWordCount() {
        return summarizedWordCount;
    }

    public double getReductionRate() {
        return reductionRate;
    }

    public void setGraphData(List<GraphNode> nodes, List<GraphLink> links) {
        this.graphNodes = nodes;
        this.graphLinks = links;
    }

    public List<GraphNode> getGraphNodes() {
        return graphNodes;
    }

    public List<GraphLink> getGraphLinks() {
        return graphLinks;
    }
}
