package org.edge.gateway;

import org.edge.gateway.models.PeerInfo;

import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerDirectory {
    private final Map<String, PeerInfo> peers = new ConcurrentHashMap<>();
    private final Clock clock;
    private final long ttlMs;

    public PeerDirectory(Clock clock, long ttlMs) {
        this.clock = clock;
        this.ttlMs = ttlMs;
    }

    public void upsert(String nodeId, String host, int port) {
        peers.put(nodeId, new PeerInfo(nodeId, host, port, clock.millis()));
    }

    public List<PeerInfo> listAlive() {
        long now = clock.millis();
        List<PeerInfo> out = new ArrayList<>();
        for (PeerInfo p : peers.values()) {
            if (now - p.lastSeenEpochMs() <= ttlMs) out.add(p);
        }
        out.sort(Comparator.comparing(PeerInfo::nodeId));
        return out;
    }

    public void prune() {
        long now = clock.millis();
        peers.entrySet().removeIf(e -> now - e.getValue().lastSeenEpochMs() > ttlMs);
    }
}
