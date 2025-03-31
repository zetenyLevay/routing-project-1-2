package ClosureAnalysis.Data.Readers;

import ClosureAnalysis.Data.Enums.POIType;
import ClosureAnalysis.Data.Models.Coordinate;
import ClosureAnalysis.Data.Models.PointOfInterest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class POIReader extends Reader{
    public POIReader(String filePath) {
        super(filePath);
    }

    @Override
    protected void readFile() throws IOException {
        try {
            // Load the GeoJSON file
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(filePath);
            JsonNode rootNode = objectMapper.readTree(file);

            // Extract the features array
            JsonNode features = rootNode.get("features");
            if (features != null && features.isArray()) {
                for (JsonNode feature : features) {
                    JsonNode properties = feature.get("properties");
                    JsonNode geometry = feature.get("geometry");
                    JsonNode coordinates = geometry.get("coordinates");

                    if (properties != null) {
                        String generalClass = properties.get("general_class").asText().toUpperCase();
                        String id = properties.get("osm_id").asText();
                        Coordinate coordinate = new Coordinate(coordinates.get(0).asDouble(), coordinates.get(1).asDouble());
                        POIType type = POIType.valueOf(generalClass);

                        PointOfInterest POI = new PointOfInterest(id, type, coordinate );
                        System.out.println(POI.getCoordinates().getLatitude() + " " + POI.getCoordinates().getLongitude());
                    }

                }
            } else {
                System.out.println("No features found in the GeoJSON file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void parseData() {

    }
}
