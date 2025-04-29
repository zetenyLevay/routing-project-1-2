package ClosureAnalysis.Calculations;

import ClosureAnalysis.Data.Graph.StopEdge;
import ClosureAnalysis.Data.Graph.StopNode;
import javafx.util.Pair;

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

        int[] stopSequences = findNeighbouringSequence(from, to);

        double start = from.getDistanceTraveledAtStop(stopSequences[0]);
        double end = to.getDistanceTraveledAtStop(stopSequences[1]);

        distanceTraveled = end - start;

        return distanceTraveled;
    }

    private double calculateTimeTaken(StopNode from, StopNode to) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        int[] stopSequences = findNeighbouringSequence(from, to);
        double timeTaken = 0;

        try {
            Date arrivalTime = formatter.parse(to.getArrivalTime(stopSequences[1]));
            Date departureTime = formatter.parse(from.getDepartureTime(stopSequences[0]));

            long diffInTime = arrivalTime.getTime() - departureTime.getTime();
            long diffInMinutes = ( diffInTime / 60000) % 60;
            double minutes = diffInMinutes;

            return minutes;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public int[] findNeighbouringSequence(StopNode from, StopNode to) {
        for (int fromSequence : from.getStopSequence()){
            for (int toSequence : to.getStopSequence()){
                if (fromSequence == toSequence-1){
                    return new int[]{fromSequence, toSequence};
                }
            }
        }
        return null;
    }
}
