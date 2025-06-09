package parsers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.leastfixedpoint.json.JSONWriter;

import routing.routingEngineModels.RouteStep;

public class CLIWrite {
    private final JSONWriter<OutputStreamWriter> responseWriter;

    public CLIWrite() {
        this.responseWriter = new JSONWriter<>(new OutputStreamWriter(System.out));
    }

    public void sendOk(Object value) throws IOException {
        responseWriter.write(Map.of("ok", value));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    public void sendError(String message) throws IOException {
        responseWriter.write(Map.of("error", message));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    public void writeRouteSteps(List<RouteStep> route) throws IOException {
        List<Map<String, Object>> steps = new ArrayList<>(route.size());
        for (RouteStep step : route) {
            steps.add(step.toJSON());
            //map.draw("type", step.getType().toString());
        }
        responseWriter.write(Map.of("ok", steps));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }
}
