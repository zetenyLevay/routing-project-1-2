package ClosureAnalysis.Calculations;

import ClosureAnalysis.Data.Graph.StopEdge;
import ClosureAnalysis.Data.Graph.StopNode;

public class EdgeWeightCalculator {

    double ALPHA = 0.5;
    double BETA = 0.5;
    public double calculateEdgeWeight(StopNode from, StopNode to) {

        double weight = 0;

        double meters = calculateDistanceTraveled(from, to);
        double timeTaken = calculateTimeTaken(from, to);

        weight = ALPHA * meters + BETA * timeTaken;
        return weight;
    }

    private double calculateDistanceTraveled(StopNode from, StopNode to) {
        double distanceTraveled = 0;

        double start = from.getDistanceTraveledAtStop();
        double end = to.getDistanceTraveledAtStop();

        distanceTraveled = end - start;

        return distanceTraveled;
    }

    private double calculateTimeTaken(StopNode from, StopNode to) {
        double timeTaken = 0;

        String arrivalTimeString = to.getArrivalTime().replace(":", "");
        String departureTimeString = from.getDepartureTime().replace(":", "");

        int arrivalTimeInt = Integer.parseInt(arrivalTimeString);
        int departureTimeInt = Integer.parseInt(departureTimeString);

        return arrivalTimeInt - departureTimeInt;
    }
}
