package ClosureAnalysis.Data.Readers;


import ClosureAnalysis.Data.Enums.POIType;

import ClosureAnalysis.Data.Models.PointOfInterest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class RoadSegmentReader extends Reader {
    public RoadSegmentReader(String filePath) {
        super(filePath);
    }

    @Override
    public void readFile() throws IOException {
    }

    @Override
    public void parseData() {

    }

    public static void main(String[] args) throws IOException {
        Reader reader = new RoadSegmentReader("data/ClosureAnalysis/POI_data.geojson");
        reader.readFile();
    }
}
