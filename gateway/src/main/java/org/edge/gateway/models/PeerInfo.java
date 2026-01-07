package org.edge.gateway.models;

public record PeerInfo(
        String nodeId,
        String host,
        int port,
        long lastSeenEpochMs
) {}
