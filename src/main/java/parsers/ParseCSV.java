package parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to parse CSV content, handling headers, quoted fields,
 * trimming, and skipping empty lines.
 */
public class ParseCSV {

    public static class Result {
        private final List<String> headers;
        private final List<String[]> records;

        public Result(List<String> headers, List<String[]> records) {
            this.headers = headers;
            this.records = records;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<String[]> getRecords() {
            return records;
        }
    }

    /**
     * Parses CSV data from the given Reader.
     * Implements first-record-as-header, ignores empty lines, and trims fields.
     */
    public static Result parse(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        // read header line (skip empty)
        do {
            line = br.readLine();
        } while (line != null && line.trim().isEmpty());

        if (line == null) {
            throw new IOException("Empty CSV input");
        }

        // strip BOM and leading whitespace
        line = line.replaceAll("^[\\uFEFF\\s]+", "");
        String[] rawHeaders = splitCsvLine(line);
        List<String> headers = new ArrayList<>();
        for (String h : rawHeaders) {
            headers.add(h.trim());
        }

        List<String[]> records = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] fields = splitCsvLine(line);
            if (fields.length != headers.size()) continue;
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
            }
            records.add(fields);
        }

        return new Result(headers, records);
    }

    /**
     * Splits a CSV line on commas not enclosed in quotes, handling escaped quotes.
     */
    private static String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}