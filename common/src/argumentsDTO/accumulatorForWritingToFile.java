package argumentsDTO;

import argumentsDTO.CommonEnums.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class accumulatorForWritingToFile implements Serializable {
    public long endTime;
    public long startTime;
    public String UserData;
    public String targetName;
    public long totalTimeToRun;
    public TargetState targetType;
    public TargetState targetState;
    public List<String> outPutData = new LinkedList<>();
    public List<String> targetOpened = new LinkedList<>();
    public List<String> SkippedTargets = new LinkedList<>();

    public accumulatorForWritingToFile() {
    }

    public accumulatorForWritingToFile(String data) {
        outPutData.add(data);
    }


    @Override
    public String toString() {
        return "accumulatorForWritingToFile{" +
                "targetName='" + targetName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", UserData='" + UserData + '\'' +
                ", targetType=" + targetType +
                ", SkippedTargets=" + SkippedTargets +
                ", targetOpened=" + targetOpened +
                ", targetState=" + targetState +
                ", totalTimeToRun=" + totalTimeToRun +
                ", outPutData=" + outPutData +
                '}';
    }
}
