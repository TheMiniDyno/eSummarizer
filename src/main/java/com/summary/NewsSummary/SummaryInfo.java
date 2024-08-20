package com.summary.NewsSummary;

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

    // Getters and Setters for existing fields

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

    // Getters and Setters for graph data

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

class SentenceRank {
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

class GraphNode {
    private int id;
    private double rank;

    public GraphNode(int id, double rank) {
        this.id = id;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public double getRank() {
        return rank;
    }
}

class GraphLink {
    private int source;
    private int target;

    public GraphLink(int source, int target) {
        this.source = source;
        this.target = target;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }
}
