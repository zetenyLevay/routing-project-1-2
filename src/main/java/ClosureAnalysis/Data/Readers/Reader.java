package ClosureAnalysis.Data.Readers;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class Reader {
    protected String filePath;

    public Reader(String filePath) {
        this.filePath = filePath;
    }

    protected abstract void readFile() throws IOException;

    protected abstract void parseData();
}
