package org.edge.gateway.agents;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AgentDirectory {

    private final Map<String, AgentRecord> agents = new ConcurrentHashMap<>();
    private final long ttlMs;

    public AgentDirectory(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    public void register(AgentRecord record) {
        record.touch();
        agents.put(record.agentId, record);
    }

    public List<AgentRecord> findByRole(String role) {
        cleanupExpired();
        return agents.values().stream()
                .filter(a -> a.roles.contains(role))
                .collect(Collectors.toList());
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        agents.values().removeIf(a -> now - a.lastSeen > ttlMs);
    }
}