// src/main/java/org/edge/gateway/http/RegisterAgentHandler.java
package org.edge.gateway.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.edge.gateway.agents.AgentDirectory;
import org.edge.gateway.agents.AgentRecord;

import java.io.IOException;

public class RegisterAgentHandler implements HttpHandler {

    private final AgentDirectory directory;
    private final ObjectMapper mapper = new ObjectMapper();

    public RegisterAgentHandler(AgentDirectory directory) {
        this.directory = directory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        AgentRecord record = mapper.readValue(
                exchange.getRequestBody(),
                AgentRecord.class
        );

        directory.register(record);

        exchange.sendResponseHeaders(200, -1);
    }
}
