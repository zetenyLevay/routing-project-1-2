package Parsers;

import java.io.IOException;
import java.io.InputStreamReader;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONSyntaxError;

public class CLIRead {
    private JSONReader requestReader;

    public CLIRead() {
        requestReader = new JSONReader(new InputStreamReader(System.in));
    }

    /**
     * Reads a JSON object from the standard input.
     * 
     * @return the parsed JSON object.
     * @throws IOException if an I/O error occurs.
     * @throws JSONSyntaxError if the input JSON is malformed.
     */
    public Object read() throws IOException, JSONSyntaxError {
        return requestReader.read();
    }
}
