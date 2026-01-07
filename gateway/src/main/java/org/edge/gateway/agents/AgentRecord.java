package org.edge.gateway.agents;


import java.util.List;

public class AgentRecord {
    public String agentId;
    public String nodeId;
    public List<String> roles;
    public List<String> capabilities;
    public long lastSeen;

    public void touch() {
        this.lastSeen = System.currentTimeMillis();
    }
}