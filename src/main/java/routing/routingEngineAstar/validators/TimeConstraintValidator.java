package routing.routingEngineAstar.validators;

/**
 * Validates time constraints for route connections
 */
public class TimeConstraintValidator {
    
    private static final int MAX_WAIT_SECONDS = 3600; // 1 hour
    
    /**
     * Checks if a connection is valid based on time constraints
     * @param arrivalTime Current step arrival time (HH:MM:SS format)
     * @param departureTime Next step departure time (HH:MM:SS format)
     * @return true if the time difference is within acceptable limits
     */
    public boolean isValidTimeConnection(String arrivalTime, String departureTime) {
        if (arrivalTime == null || departureTime == null) {
            return false;
        }
        
        try {
            int arrivalSeconds = timeToSeconds(arrivalTime);
            int departureSeconds = timeToSeconds(departureTime);
            
            // Handle day rollover (departure next day)
            if (departureSeconds < arrivalSeconds) {
                departureSeconds += 24 * 3600; // Add 24 hours
            }
            
            int waitTime = departureSeconds - arrivalSeconds;
            return waitTime >= 0 && waitTime <= MAX_WAIT_SECONDS;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Converts time string to seconds since midnight
     */
    private int timeToSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        
        return hours * 3600 + minutes * 60 + seconds;
    }
    
    /**
     * Gets the wait time in seconds between two time points
     */
    public int getWaitTimeSeconds(String arrivalTime, String departureTime) {
        if (!isValidTimeConnection(arrivalTime, departureTime)) {
            return -1;
        }
        
        int arrivalSeconds = timeToSeconds(arrivalTime);
        int departureSeconds = timeToSeconds(departureTime);
        
        if (departureSeconds < arrivalSeconds) {
            departureSeconds += 24 * 3600;
        }
        
        return departureSeconds - arrivalSeconds;
    }
}
