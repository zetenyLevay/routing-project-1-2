package ClosureAnalysis.Data.Readers;

import ClosureAnalysis.Data.Enums.POIType;

import ClosureAnalysis.Data.Models.PointOfInterest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class POIReader extends Reader{
    private JsonNode rootNode;
    private List<PointOfInterest> pointOfInterests = new ArrayList<>();
    public POIReader(String filePath) {
        super(filePath);
    }

    @Override
    public void readFile() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(filePath);
            rootNode = objectMapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void parseData() {

        JsonNode features = rootNode.get("features");
        if (features != null && features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode properties = feature.get("properties");
                JsonNode geometry = feature.get("geometry");
                JsonNode coordinates = geometry.get("coordinates");

                if (properties != null) {
                    String generalClass = properties.get("general_class").asText().toUpperCase();
                    String id = properties.get("osm_id").asText();

                    Double lat = coordinates.get(0).asDouble();
                    Double lng = coordinates.get(1).asDouble();
                    List<Double> coordinate = new ArrayList<>();
                    coordinate.add(lat);
                    coordinate.add(lng);

                    POIType type = POIType.valueOf(generalClass);

                    PointOfInterest POI = new PointOfInterest(id, type, coordinate );
                    pointOfInterests.add(POI);
                }
            }
        } else {
            System.out.println("No features found in the GeoJSON file.");
        }
    }

    public List<PointOfInterest> getPointOfInterests() {
        return pointOfInterests;
    }
}
