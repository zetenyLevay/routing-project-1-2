package ClosureAnalysis.Data.Readers;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class Reader {
    protected String filePath;

    public Reader(String filePath) {
        this.filePath = filePath;
    }

    public abstract void readFile() throws IOException;

    public abstract void parseData();

}
