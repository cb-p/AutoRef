package nl.roboteamtwente.autoref.model;

public record Touch(int id, Vector3 startLocation, Vector3 endLocation, double startTime, Double endTime, RobotIdentifier by) {
    public boolean isFinished() {
        return this.endLocation != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Touch other) {
            return id == other.id;
        } else {
            return false;
        }
    }
}
