package ClosureAnalysis.Data.Readers;


import java.io.IOException;

public class RoadSegmentReader extends Reader {
    public RoadSegmentReader(String filePath) {
        super(filePath);
    }

    @Override
    protected void readFile() throws IOException {


    }

    @Override
    protected void parseData() {

    }

    public static void main(String[] args) throws IOException {
        Reader reader = new RoadSegmentReader("data/ClosureAnalysis/RoadUsage/ReprojectedRoadData.geojson");
        reader.readFile();
    }
}
