package com.summary.eSummarizer.DTO;

public class GraphNode {
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
