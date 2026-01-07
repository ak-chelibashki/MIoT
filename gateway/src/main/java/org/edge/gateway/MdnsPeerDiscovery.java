package org.edge.gateway;

import javax.jmdns.*;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class MdnsPeerDiscovery implements AutoCloseable {
    public static final String SERVICE_TYPE = "_edgegw._tcp.local.";

    private final JmDNS jmdns;
    private final PeerDirectory directory;

    public MdnsPeerDiscovery(String nodeId, int port, PeerDirectory directory) throws IOException {
        this.directory = directory;

        // Bind to default interface (host network container => LAN interface)
        this.jmdns = JmDNS.create(InetAddress.getLocalHost(), nodeId);

        // Advertise self
        ServiceInfo self = ServiceInfo.create(
                SERVICE_TYPE,
                "edgegw-" + nodeId,
                port,
                0,
                0,
                "nodeId=" + nodeId
        );
        jmdns.registerService(self);

        // Discover others
        jmdns.addServiceListener(SERVICE_TYPE, new ServiceListener() {
            @Override public void serviceAdded(ServiceEvent event) {
                // Request resolution
                jmdns.requestServiceInfo(event.getType(), event.getName(), true);
            }

            @Override public void serviceRemoved(ServiceEvent event) {
                // TTL pruning handles disappearance; nothing needed
            }

            @Override public void serviceResolved(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                String peerNodeId = info.getPropertyString("nodeId");
                if (peerNodeId == null || peerNodeId.isBlank()) return;

                String host = firstHost(info);
                int peerPort = info.getPort();
                if (host != null && peerPort > 0) {
                    directory.upsert(peerNodeId, host, peerPort);
                }
            }
        });
    }

    private static String firstHost(ServiceInfo info) {
        String[] addrs = info.getHostAddresses();
        if (addrs != null && addrs.length > 0) return addrs[0];
        String server = info.getServer();
        return server == null ? null : server.replaceAll("\\.$", "");
    }

    @Override
    public void close() throws IOException {
        jmdns.close();
    }
}
