package org.edge.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.edge.gateway.models.PeerInfo;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GatewayApp {
    public static void main(String[] args) throws Exception {
        String nodeId = env("NODE_ID", "node-unknown");
        int port = Integer.parseInt(env("GATEWAY_PORT", "7000"));
        long ttlMs = Long.parseLong(env("PEER_TTL_MS", "30000"));

        ObjectMapper om = new ObjectMapper();
        PeerDirectory directory = new PeerDirectory(Clock.systemUTC(), ttlMs);

        // mDNS advertise + discover
        try (MdnsPeerDiscovery mdns = new MdnsPeerDiscovery(nodeId, port, directory)) {

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/health", exchange -> {
                byte[] body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
            });

            server.createContext("/peers", exchange -> {
                if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                List<PeerInfo> peers = directory.listAlive();
                byte[] body = om.writeValueAsBytes(peers);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(directory::prune, 5, 5, TimeUnit.SECONDS);

            System.out.printf("[gateway] nodeId=%s port=%d ttlMs=%d%n", nodeId, port, ttlMs);

            // keep running
            Thread.currentThread().join();
        }
    }

    private static String env(String k, String d) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? d : v;
    }
}
