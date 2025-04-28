package ClosureAnalysis.Calculations;

import ClosureAnalysis.Data.Graph.StopEdge;
import ClosureAnalysis.Data.Graph.StopNode;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        double timeTaken = 0;

        try {
            Date arrivalTime = formatter.parse(to.getArrivalTime());
            Date departureTime = formatter.parse(from.getDepartureTime());

            long diffInTime = arrivalTime.getTime() - departureTime.getTime();
            long diffInMinutes = ( diffInTime / 60000) % 60;
            double minutes = diffInMinutes;

            return minutes;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
