package dataTransferObjects;

import argumentsDTO.CommonEnums.*;

public class GraphInfoDTO {
    private int totalNumberOfRoots;
    private int totalNumberOfLeaves;
    private int totalNumberOfMiddles;
    private int totalNumberOfTargets;
    private int totalNumberOfIndependents;
    private String graphName;
    private String ownerName;
    private int simulationPrice;
    private int compilationPrice;

    public GraphInfoDTO(int totalNumberOfTargets, int totalNumberOfLeaves,
                        int totalNumberOfMiddles, int totalNumberOfRoots,
                        int totalNumberOfIndependents) {
        this.totalNumberOfTargets = totalNumberOfTargets;
        this.totalNumberOfLeaves = totalNumberOfLeaves;
        this.totalNumberOfMiddles = totalNumberOfMiddles;
        this.totalNumberOfRoots = totalNumberOfRoots;
        this.totalNumberOfIndependents = totalNumberOfIndependents;
    }

    public void countTargetsByType(TargetType type) {
        totalNumberOfTargets++;
        switch (type) {
            case LEAF:
                totalNumberOfLeaves++;
                break;
            case MIDDLE:
                totalNumberOfMiddles++;
                break;
            case ROOT:
                totalNumberOfRoots++;
                break;
            case INDEPENDENT:
                totalNumberOfIndependents++;
                break;
        }
    }

    public GraphInfoDTO() {
        this(0, 0, 0, 0, 0);
    }

    public int getTotalNumberOfTargets() {
        return totalNumberOfTargets;
    }

    public int getTotalNumberOfRoots() {
        return totalNumberOfRoots;
    }

    public int getTotalNumberOfLeaves() {
        return totalNumberOfLeaves;
    }

    public int getTotalNumberOfMiddles() {
        return totalNumberOfMiddles;
    }

    public int getTotalNumberOfIndependents() {
        return totalNumberOfIndependents;
    }

    public String getGraphName() {
        return graphName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public int getSimulationPrice() {
        return simulationPrice;
    }

    public int getCompilationPrice() {
        return compilationPrice;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setSimulationPrice(int simulationPrice) {
        this.simulationPrice = simulationPrice;
    }

    public void setCompilationPrice(int compilationPrice) {
        this.compilationPrice = compilationPrice;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Distribution of targets by type:")
                .append("\n Total Number Of Targets: ").append(totalNumberOfTargets)
                .append("\n Total Number Of Roots: ").append(totalNumberOfRoots)
                .append("\n Total number of leaves: ").append(totalNumberOfLeaves)
                .append("\n Total Number Of Middles: ").append(totalNumberOfMiddles)
                .append("\n Total Number Of Independents: ").append(totalNumberOfIndependents);
        return sb.toString();
    }
}
