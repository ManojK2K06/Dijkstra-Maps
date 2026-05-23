package com.pathfinder.graph;

import com.pathfinder.model.Edge;
import java.util.*;

public class GraphManager {
    private Map<Integer, List<Edge>> adjacencyList = new HashMap<>();

    public void clear() {
        adjacencyList.clear();
    }

    public void addNode(int cityId) {
        adjacencyList.putIfAbsent(cityId, new ArrayList<>());
    }

    public void addEdge(Edge edge) {
        adjacencyList.computeIfAbsent(edge.getSourceId(), k -> new ArrayList<>()).add(edge);
    }

    // Dijkstra's Algorithm
    public List<Integer> getShortestPath(int startId, int endId) {
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        Map<Integer, Double> minDistance = new HashMap<>();
        Map<Integer, Integer> previousNode = new HashMap<>();

        for (Integer cityId : adjacencyList.keySet()) {
            minDistance.put(cityId, Double.MAX_VALUE);
        }

        minDistance.put(startId, 0.0);
        pq.add(new NodeDistance(startId, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int currentId = current.id;

            if (currentId == endId)
                break;
            if (current.distance > minDistance.get(currentId))
                continue;

            for (Edge edge : adjacencyList.getOrDefault(currentId, Collections.emptyList())) {
                double newDist = minDistance.get(currentId) + edge.getDistance();
                if (newDist < minDistance.get(edge.getTargetId())) {
                    minDistance.put(edge.getTargetId(), newDist);
                    previousNode.put(edge.getTargetId(), currentId);
                    pq.add(new NodeDistance(edge.getTargetId(), newDist));
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        Integer curr = endId;
        if (!previousNode.containsKey(curr) && curr != startId)
            return path;

        while (curr != null) {
            path.add(0, curr);
            curr = previousNode.get(curr);
        }
        return path;
    }

    private static class NodeDistance {
        int id;
        double distance;

        NodeDistance(int id, double distance) {
            this.id = id;
            this.distance = distance;
        }
    }
}