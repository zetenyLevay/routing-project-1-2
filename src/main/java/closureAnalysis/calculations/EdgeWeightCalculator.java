package closureAnalysis.calculations;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closureAnalysis.data.graph.StopInstance;
import closureAnalysis.data.graph.StopNode;

/**
 * Calculates weights for edges between stops in a transportation network.
 * Weights combine both distance and time factors between connected stops.
 */
public class EdgeWeightCalculator {

    double ALPHA = 0.1;
    double BETA = 0.9;
    int count = 0;


    /**
     * Calculates the edge weight between two stop nodes.
     * The weight is a combination of distance (ALPHA-weighted) and time (BETA-weighted).
     *
     * @param from The starting stop node
     * @param to The ending stop node
     * @return The calculated edge weight, or Double.NEGATIVE_INFINITY if calculation fails
     */
    public double calculateEdgeWeight(StopNode from, StopNode to) {

        List<StopInstance> instances = neighboringInstances(from, to);

        if (instances == null){
            System.err.println("No neighbor for from: " + from.getId() + " to " + to.getId());
            return Double.NEGATIVE_INFINITY;
        }

        double meters = calculateDistanceTraveled(instances.getFirst(), instances.getLast());
        double minutes = calculateTimeTaken(instances.getFirst(),instances.getLast());


        if (meters < 0 || minutes < 0) {
            System.out.println("Weight issue from " + from.getId() + " to " + to.getId());
            System.out.println("  Distance: " + meters + " | Time: " + minutes);
            count++;
            System.err.println(count);
            return Double.NEGATIVE_INFINITY;

        }


        return  ALPHA * meters + BETA * minutes;
    }

    /**
     * Calculates the distance traveled between two stop instances.
     *
     * @param from The starting stop instance
     * @param to The ending stop instance
     * @return Distance traveled between stops in meters
     */
    public double calculateDistanceTraveled(StopInstance from, StopInstance to) {
        return to.getDistanceTraveled() - from.getDistanceTraveled();
    }

    /**
     * Finds neighboring stop instances within the same trip.
     *
     * @param from The starting stop node
     * @param to The ending stop node
     * @return List of StopInstances in sequence, or null if no valid sequence found
     */
    private List<StopInstance> neighboringInstances(StopNode from, StopNode to){
        List<StopInstance> neighboringInstances = new ArrayList<>();


            for (StopInstance fInstance : from.getStopInstances()) {
                for (StopInstance tInstance : to.getStopInstances()) {

                    if (fInstance.getTripId().equals(tInstance.getTripId())) {
                        if (fInstance.getStopSequence() == tInstance.getStopSequence() - 1) {
                            neighboringInstances.add(fInstance);
                            neighboringInstances.add(tInstance);
                            return neighboringInstances;
                        }
                    }
                }

        }
        return null;
    }
    /**
     * Calculates the time taken between departure and arrival at two stops.
     *
     * @param departure The departure stop instance
     * @param arrival The arrival stop instance
     * @return Time taken in minutes, or Double.POSITIVE_INFINITY if time parsing fails
     */
    public double calculateTimeTaken(StopInstance departure, StopInstance arrival) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        try {
            Date departureTime = formatter.parse(departure.getDepartureTime());
            Date arrivalTime = formatter.parse(arrival.getArrivalTime());
            long diff = arrivalTime.getTime() - departureTime.getTime();
            return diff / 60000.0; // convert ms to minutes
        } catch (ParseException e) {
            System.err.println("Failed to parse time: " + departure + " or " + arrival);
            return Double.POSITIVE_INFINITY;
        }
    }
}
