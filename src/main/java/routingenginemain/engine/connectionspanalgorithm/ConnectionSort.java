package routingenginemain.engine.connectionspanalgorithm;

import routingenginemain.model.Connection;

import java.util.Comparator;
import java.util.List;

public class ConnectionSort {
    public static List<Connection> sortConnections(List<Connection> connections) {
        return connections.parallelStream()
                .sorted(Comparator.comparingInt(Connection::getDepTime))
                .toList();
    }
}
