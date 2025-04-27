package RoutingEngineDijkstra;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONWriter; //Debugging

public class RoutingEngine {

    private JSONReader requestReader
            = new JSONReader(new InputStreamReader(System.in));
    private JSONWriter<OutputStreamWriter> responseWriter
            = new JSONWriter<>(new OutputStreamWriter(System.out));

    //TODO: implement dijkstra algorithm
    public void run() throws IOException {

    }

}
