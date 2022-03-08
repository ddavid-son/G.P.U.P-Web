package argumentsDTO;

import argumentsDTO.CommonEnums.*;

public class CompilationArgs extends TaskArgs {

    private String srcPath;
    private String dstPath;

    public CompilationArgs(boolean isWhatIf, RelationType relationType, String srcPath, String dstPath) {
        super(isWhatIf, CommonEnums.TaskType.COMPILATION, relationType);
        this.dstPath = dstPath;
        this.srcPath = srcPath;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public String getDstPath() {
        return dstPath;
    }

    public TaskArgs cloneArgs() {
        CompilationArgs s = new CompilationArgs(isWhatIf, relationType, srcPath, dstPath);
        s.taskName = taskName;
        s.taskOwner = taskOwner;
        s.originalGraph = originalGraph;
        s.targetsSelectedForGraph.addAll(targetsSelectedForGraph);
        return s;
    }
}
