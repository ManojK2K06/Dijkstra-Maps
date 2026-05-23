package com.pathfinder.model;

public class Edge {
    private int sourceId;
    private int targetId;
    private double distance;

    public Edge(int sourceId, int targetId, double distance) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.distance = distance;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public double getDistance() {
        return distance;
    }
}