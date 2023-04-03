package nl.roboteamtwente.autoref.model;

public record Touch(int id, Vector3 startLocation, Vector3 endLocation, double startTime, Double endTime, Vector3 startVelocity, Vector3 endVelocity, RobotIdentifier by) {
    public boolean isFinished() {
        return this.endLocation != null;
    }

    public float deflectionAngle() {
        float angle = startVelocity.xy().angle(endVelocity.xy());
        return Math.min(angle, 360 - angle);
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
