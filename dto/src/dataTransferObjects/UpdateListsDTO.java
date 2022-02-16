package dataTransferObjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateListsDTO {
    public Set<String> frozenSet = new HashSet<>();
    public Set<String> warningSet = new HashSet<>();
    public Set<String> skippedSet = new HashSet<>();
    public Set<String> SuccessSet = new HashSet<>();
    public Set<String> inProcessSet = new HashSet<>();
    public Set<String> waitingSet = new HashSet<>();
    public Set<String> failedSet = new HashSet<>();

    public void setAll(Set<String> frozenSet, Set<String> warningSet, Set<String> skippedSet,
                       Set<String> successSet, Set<String> inProcessSet, Set<String> waitingSet,
                       Set<String> failedSet) {

        this.frozenSet.clear();
        this.frozenSet.addAll(frozenSet);

        this.warningSet.addAll(warningSet);
        this.skippedSet.addAll(skippedSet);
        this.SuccessSet.addAll(successSet);
        this.failedSet.addAll(failedSet);

        this.inProcessSet.clear();
        this.inProcessSet.addAll(inProcessSet);

        this.waitingSet.clear();
        this.waitingSet.addAll(waitingSet);
    }

    public List<String> getFrozenAsList() {
        return new ArrayList<>(frozenSet);
    }

    public List<String> getWarningAsList() {
        return new ArrayList<>(warningSet);
    }

    public List<String> getSkippedAsList() {
        return new ArrayList<>(skippedSet);
    }

    public List<String> getSuccessAsList() {
        return new ArrayList<>(SuccessSet);
    }

    public List<String> getInProcessAsList() {
        return new ArrayList<>(inProcessSet);
    }

    public List<String> getWaitingAsList() {
        return new ArrayList<>(waitingSet);
    }

    public List<String> getFailedAsList() {
        return new ArrayList<>(failedSet);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Set<String> getFrozenSet() {
        return frozenSet;
    }

    public void setFrozenSet(Set<String> frozenSet) {
        this.frozenSet = frozenSet;
    }

    public Set<String> getWarningSet() {
        return warningSet;
    }

    public void setWarningSet(Set<String> warningSet) {
        this.warningSet = warningSet;
    }

    public Set<String> getSkippedSet() {
        return skippedSet;
    }

    public void setSkippedSet(Set<String> skippedSet) {
        this.skippedSet = skippedSet;
    }

    public Set<String> getSuccessSet() {
        return SuccessSet;
    }

    public void setSuccessSet(Set<String> successSet) {
        SuccessSet = successSet;
    }

    public Set<String> getInProcessSet() {
        return inProcessSet;
    }

    public void setInProcessSet(Set<String> inProcessSet) {
        this.inProcessSet = inProcessSet;
    }

    public Set<String> getWaitingSet() {
        return waitingSet;
    }

    public void setWaitingSet(Set<String> waitingSet) {
        this.waitingSet = waitingSet;
    }

    public Set<String> getFailedSet() {
        return failedSet;
    }

    public void setFailedSet(Set<String> failedSet) {
        this.failedSet = failedSet;
    }
}
