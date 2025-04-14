package com.group.routingenginemain.model.pathway;

import com.group.routingenginemain.model.Stop;

public class Pathway {
    private final String pathwayID;
    private final Stop fromStop;
    private final Stop toStop;
    private final PathwayType pathwayType;
    private final int traversalTime;

    public Pathway(String pathwayID, Stop fromStop, Stop toStop, int code, int time) {
        this.pathwayID = pathwayID;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.pathwayType = PathwayType.getNameFromCode(code);
        this.traversalTime = time;
    }


    public String getPathwayID() {
        return pathwayID;
    }

    public Stop getFromStop() {
        return fromStop;
    }

    public Stop getToStop() {
        return toStop;
    }

    public String getPathwayTypeName() {
        return pathwayType.showDisplayName();
    }

    public int getTraversalTime() {
        return traversalTime;
    }
}
