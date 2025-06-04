// CLIWrite.java
package parsers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.leastfixedpoint.json.JSONWriter;

import routing.routingEngineModels.RouteStep;

public class CLIWrite {

    private JSONWriter<OutputStreamWriter> responseWriter;

    public CLIWrite() {
        responseWriter = new JSONWriter<>(new OutputStreamWriter(System.out));
    }

    /**
     * Sends a successful JSON response.
     *
     * @param value the value to include in the "ok" response.
     * @throws IOException if an I/O error occurs.
     */
    public void sendOk(Object value) throws IOException {
        responseWriter.write(Map.of("ok", value));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    /**
     * Sends an error JSON response.
     *
     * @param message the error message.
     * @throws IOException if an I/O error occurs.
     */
    public void sendError(String message) throws IOException {
        responseWriter.write(Map.of("error", message));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    public void writeRouteSteps(List<RouteStep> route) throws IOException {
        // JSON obj = JSON.parse(route);
        // JsonObject outer = JsonParser.parseString(jsonString).getAsJsonObject();

        // System.out.println(route);
        // // responseWriter.write(route);
        responseWriter.write(Map.of("ok", route.toString()));

      
    }
}
