package com.summary.eSummarizer.DTO;

public class GraphLink {
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
