package dataTransferObjects;

import argumentsDTO.TaskTarget;
import argumentsDTO.accumulatorForWritingToFile;

public class WorkHistoryDto {

    public int lastIndex;
    private TaskTarget finishedWork;
    private accumulatorForWritingToFile finishedLogs;

    public WorkHistoryDto(TaskTarget finishedWork, accumulatorForWritingToFile finishedLogs) {
        this.finishedWork = finishedWork;
        this.finishedLogs = finishedLogs;
    }


    public String getTaskName() {
        return finishedWork.taskName;
    }

    public String getTaskType() {
        return finishedWork.taskType.toString();
    }

    public String getTargetName() {
        return finishedWork.name;
    }

    public String getTargetLogs() {
        return String.join("\n", finishedLogs.outPutData);
    }

    public Integer getTargetValue() {
        return finishedWork.targetPrice;
    }

    public String getTargetState() {
        return finishedWork.getState().toString();
    }
}
