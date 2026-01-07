// src/main/java/org/edge/gateway/http/FindAgentsHandler.java
package org.edge.gateway.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.edge.gateway.agents.AgentDirectory;

import java.io.IOException;
import java.util.Map;

public class FindAgentsHandler implements HttpHandler {

    private final AgentDirectory directory;
    private final ObjectMapper mapper = new ObjectMapper();

    public FindAgentsHandler(AgentDirectory directory) {
        this.directory = directory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("role=")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String role = query.substring("role=".length());

        byte[] response = mapper.writeValueAsBytes(
                directory.findByRole(role)
        );

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
