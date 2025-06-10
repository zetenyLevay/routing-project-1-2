package routing.routingEngineDijkstra.dijkstra.converters;

import routing.routingEngineDijkstra.adiModels.AdiRouteInfo;
import routing.routingEngineDijkstra.adiModels.AdiRouteStep;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.dijkstra.model.input.*;
import routing.routingEngineDijkstra.dijkstra.model.output.*;
import routing.routingEngineModels.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for converting between routing engine models and Dijkstra-specific models.
 */
public class DijkstraModelConverter {

    //really regretting this
    /**
     * Converts an InputJourney to a DijkstraInputJourney.
     *
     * @param inputJourney the input journey to convert
     * @return a DijkstraInputJourney object
     */
    public static DijkstraInputJourney toDijkstraInputJourney(InputJourney inputJourney) {
        return new DijkstraInputJourney(
                toDijkstraCoordinates(inputJourney.getStart()),
                toDijkstraCoordinates(inputJourney.getEnd()),
                inputJourney.getStartTime()
        );
    }

    /**
     * Converts Coordinates to DijkstraCoordinates.
     *
     * @param coordinates the coordinates to convert
     * @return a DijkstraCoordinates object
     */
    public static DijkstraCoordinates toDijkstraCoordinates(Coordinates coordinates) {
        return new DijkstraCoordinates(coordinates.getLatitude(), coordinates.getLongitude());
    }

    /**
     * Converts an AdiStop to a DijkstraStop.
     *
     * @param stop the stop to convert
     * @return a DijkstraStop object
     */
    public static DijkstraStop toDijkstraStop(AdiStop stop) {
        return new DijkstraStop(
                stop.getStopID(),
                stop.getStopName(),
                stop.getLatitude(),
                stop.getLongitude()
        );
    }

    /**
     * Converts a DijkstraFinalRoute to a FinalRoute, adjusting times based on the journey start time.
     *
     * @param dijkstraFinalRoute the Dijkstra route to convert
     * @param journeyStartTime   the start time of the journey
     * @return a FinalRoute object
     */
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

    /**
     * Converts a DijkstraRouteStep to an AdiRouteStep, handling both walking and transit steps.
     *
     * @param dijkstraStep the Dijkstra route step to convert
     * @param startTime the start time of the step
     * @return an AdiRouteStep object
     */
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

    /**
     * Converts DijkstraCoordinates to Coordinates.
     *
     * @param dijkstraCoordinates the Dijkstra coordinates to convert
     * @return a Coordinates object
     */
    public static Coordinates toCoordinates(DijkstraCoordinates dijkstraCoordinates) {
        return new Coordinates(dijkstraCoordinates.getLatitude(), dijkstraCoordinates.getLongitude());
    }

    /**
     * Converts a DijkstraRouteInfo to an AdiRouteInfo.
     *
     * @param dijkstraRouteInfo the Dijkstra route info to convert
     * @return an AdiRouteInfo object, or null if the input is null
     */
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