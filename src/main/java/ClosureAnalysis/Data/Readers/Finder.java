package ClosureAnalysis.Data.Readers;

import java.util.List;

public interface Finder<I, O> {
    List<O> find(I input);
}
