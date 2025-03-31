import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONSyntaxError;
import com.leastfixedpoint.json.JSONWriter;

public class RoutingEngine {
    private JSONReader requestReader =
        new JSONReader(new InputStreamReader(System.in));
    private JSONWriter<OutputStreamWriter> responseWriter =
        new JSONWriter<>(new OutputStreamWriter(System.out));

    public static void main(String[] args) throws IOException {
        new RoutingEngine().run();
    }

    public void run() throws IOException {
        System.err.println("Starting");
        while (true) {
            Object json;
            try {
                json = requestReader.read();
            } catch (JSONSyntaxError e) {
                sendError("Bad JSON input");
                break;
            } catch (EOFException e) {
                System.err.println("End of input detected");
                break;
            }

            if (json instanceof Map<?,?>) {
                Map<?,?> request = (Map<?,?>) json;
                if (request.containsKey("ping")) {
                    sendOk(Map.of("pong", request.get("ping")));
                    continue;
                }
                // ... process other requests here
                //load 
                //make ping pong like test cases
                //be able to convert from json to 
                //you need to turn the json into java objects
                //make bash scripts , msys2 for windows, git bash
                // run basic hello worlds file containing {"ping":kkf}

                //CI gitlab actions to run tests automatically when pushing, check if theyre enabled.
                //java tests
            }

            sendError("Bad request");
        }
    }

    private void sendOk(Object value) throws IOException {
        responseWriter.write(Map.of("ok", value));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    private void sendError(String message) throws IOException {
        responseWriter.write(Map.of("error", message));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }
}
