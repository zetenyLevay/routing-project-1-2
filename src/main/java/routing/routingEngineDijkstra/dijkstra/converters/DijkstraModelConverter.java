package routing.routingEngineDijkstra.dijkstra.converters;

import routing.routingEngineDijkstra.dijkstra.model.input.*;
import routing.routingEngineDijkstra.dijkstra.model.output.*;
import routing.routingEngineModels.*;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DijkstraModelConverter {

    // Convert from normal models to Dijkstra models
    public static DijkstraInputJourney toDijkstraInputJourney(InputJourney inputJourney) {
        return new DijkstraInputJourney(
                toDijkstraCoordinates(inputJourney.getStart()),
                toDijkstraCoordinates(inputJourney.getEnd()),
                inputJourney.getStartTime()
        );
    }

    public static DijkstraCoordinates toDijkstraCoordinates(Coordinates coordinates) {
        return new DijkstraCoordinates(coordinates.getLatitude(), coordinates.getLongitude());
    }

    public static DijkstraStop toDijkstraStop(Stop stop) {
        return new DijkstraStop(
                stop.getStopID(),
                stop.getStopName(),
                stop.getLatitude(),
                stop.getLongitude()
        );
    }

    // Convert from Dijkstra models to normal models
    public static FinalRoute toFinalRoute(DijkstraFinalRoute dijkstraFinalRoute, LocalTime journeyStartTime) {
        List<RouteStep> routeSteps = new ArrayList<>();
        LocalTime currentTime = journeyStartTime;

        for (Object step : dijkstraFinalRoute.getRouteSteps()) {
            DijkstraRouteStep dijkstraStep = (DijkstraRouteStep) step;
            RouteStep convertedStep = toRouteStep(dijkstraStep, currentTime);
            routeSteps.add(convertedStep);
            currentTime = currentTime.plusSeconds((long)(dijkstraStep.getTime() * 60));
        }

        return new FinalRoute(
                new ArrayList<>(routeSteps),
                dijkstraFinalRoute.getTotalDistance(),
                dijkstraFinalRoute.getTotalTime()
        );
    }

    public static RouteStep toRouteStep(DijkstraRouteStep dijkstraStep, LocalTime startTime) {
        Coordinates to = toCoordinates(dijkstraStep.getEndCoord());
        double duration = dijkstraStep.getTime();

        if ("WALK".equalsIgnoreCase(dijkstraStep.getModeOfTransport())) {
            return new RouteStep(
                    "walk",
                    to,
                    duration,
                    startTime
            );
        } else {
            return new RouteStep(
                    "ride",
                    to,
                    duration,
                    startTime,
                    dijkstraStep.getStopName(),
                    toRouteInfo(dijkstraStep.getRouteInfo())
            );
        }
    }

    public static Coordinates toCoordinates(DijkstraCoordinates dijkstraCoordinates) {
        return new Coordinates(dijkstraCoordinates.getLatitude(), dijkstraCoordinates.getLongitude());
    }

    public static RouteInfo toRouteInfo(DijkstraRouteInfo dijkstraRouteInfo) {
        if (dijkstraRouteInfo == null) return null;
        return new RouteInfo(
                dijkstraRouteInfo.operator,
                dijkstraRouteInfo.shortName,
                dijkstraRouteInfo.longName,
                dijkstraRouteInfo.headSign
        );
    }
}