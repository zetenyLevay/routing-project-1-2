package routing.routingEngineCSA.engine.connectionscanalgorithm;

import routing.routingEngineModels.Connection;
import java.util.Comparator;
import java.util.List;

public class ConnectionSort {
    public static List<Connection> sortConnections(List<Connection> connections) {
        return connections.parallelStream()
                .sorted(Comparator.comparing(Connection::getDepTime))
                .toList();
    }
}