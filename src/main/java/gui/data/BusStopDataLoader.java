package gui.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BusStopDataLoader {        //Load bus stop location data from CSV file. Converts coordinate strings to LocationPoint objects
    public static List<LocationPoint> loadFromCsvFile(String csvFilePath) {
        List<LocationPoint> busStopsList = new ArrayList<>();
        String csvSeparatorRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader fileReader = new BufferedReader(new FileReader(csvFilePath))) {
            fileReader.readLine();
            
            String currentLine;
            while ((currentLine = fileReader.readLine()) != null) {
                String[] csvColumns = currentLine.split(csvSeparatorRegex, -1);
                
                if (csvColumns.length < 4) continue;
                
                try {
                    double latitude = Double.parseDouble(csvColumns[2]);
                    double longitude = Double.parseDouble(csvColumns[3]);
                    busStopsList.add(new LocationPoint(latitude, longitude));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException fileError) {
            System.err.println("Error reading CSV file: " + fileError.getMessage());
        }

        return busStopsList;
    }
}