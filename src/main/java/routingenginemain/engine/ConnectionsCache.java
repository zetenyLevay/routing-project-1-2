package routingenginemain.engine;

import routingenginemain.model.Connection;

import java.util.List;

public class ConnectionsCache {
    private static final List<Connection> SORTED_CONNECTIONS;

    static {
        List<Connection> connections = loadConnectionsFromGTFS();
        SORTED_CONNECTIONS = ConnectionSort.sortConnections(connections);

    }

    public static List<Connection> getSortedConnections() {
        return SORTED_CONNECTIONS;
    }

    private static List<Connection> loadConnectionsFromGTFS() {
        return null;
    }
}
