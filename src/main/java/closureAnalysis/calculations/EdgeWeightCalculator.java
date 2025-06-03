package closureAnalysis.calculations;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closureAnalysis.data.graph.StopInstance;
import closureAnalysis.data.graph.StopNode;

public class EdgeWeightCalculator {

    double ALPHA = 0.1;
    double BETA = 0.9;
    int count = 0;

    /**
     *
     * @param from
     * @param to
     * @return the edge's weight between from and to
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

    public double calculateDistanceTraveled(StopInstance from, StopInstance to) {
        return to.getDistanceTraveled() - from.getDistanceTraveled();
    }

    /**
     *
     * @param from
     * @param to
     * @return the list of StopInstances within the same trip
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
