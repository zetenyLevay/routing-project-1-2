package routing.routingEngineDijkstra.dijkstra.converters;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.dijkstra.model.input.*;
import routing.routingEngineDijkstra.dijkstra.model.output.*;
import routing.routingEngineModels.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DijkstraModelConverter {

    //really regretting this
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

    public static DijkstraStop toDijkstraStop(AdiStop stop) {
        return new DijkstraStop(
                stop.getStopID(),
                stop.getStopName(),
                stop.getLatitude(),
                stop.getLongitude()
        );
    }
    public static FinalRoute toFinalRoute(DijkstraFinalRoute dijkstraFinalRoute, LocalTime journeyStartTime) {
        List<AdiRouteStep> routeSteps = new ArrayList<>();
        LocalTime currentTime = journeyStartTime;

        for (Object step : dijkstraFinalRoute.getRouteSteps()) {
            DijkstraRouteStep dijkstraStep = (DijkstraRouteStep) step;
            AdiRouteStep convertedStep = toRouteStep(dijkstraStep, currentTime);
            routeSteps.add(convertedStep);
            currentTime = currentTime.plusSeconds((long)(dijkstraStep.getTime() * 60));
        }

        return new FinalRoute(
                new ArrayList<>(routeSteps),
                dijkstraFinalRoute.getTotalDistance(),
                dijkstraFinalRoute.getTotalTime()
        );
    }

    public static AdiRouteStep toRouteStep(DijkstraRouteStep dijkstraStep, LocalTime startTime) {
        Coordinates to = toCoordinates(dijkstraStep.getEndCoord());
        double duration = dijkstraStep.getTime();

        if ("WALK".equalsIgnoreCase(dijkstraStep.getModeOfTransport())) {
            return new AdiRouteStep(
                    "walk",
                    to,
                    duration,
                    startTime
            );
        } else {
            return new AdiRouteStep(
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

    public static AdiRouteInfo toRouteInfo(DijkstraRouteInfo dijkstraRouteInfo) {
        if (dijkstraRouteInfo == null) return null;
        return new AdiRouteInfo(
                dijkstraRouteInfo.operator,
                dijkstraRouteInfo.shortName,
                dijkstraRouteInfo.longName,
                dijkstraRouteInfo.headSign
        );
    }
}