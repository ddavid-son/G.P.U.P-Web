package argumentsDTO;

public class CommonEnums {

    public enum TargetState {WAITING, SUCCESS, WARNING, FAILURE, FROZEN, IN_PROCESS, SKIPPED, FINISHED}

    public enum TargetType {LEAF, MIDDLE, ROOT, INDEPENDENT}

    public enum TaskType {
        SIMULATION,
        COMPILATION,
    }

    public enum RelationType {
        DEPENDS_ON,
        REQUIRED_FOR
    }

    public enum TaskStatus {
        NEW,     // CREATED
        ACTIVE,  // PLAYED
        PAUSED,
        CANCELED,
        FINISHED
    }

}
