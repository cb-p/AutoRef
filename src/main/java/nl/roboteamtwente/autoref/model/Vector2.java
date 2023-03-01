package nl.roboteamtwente.autoref.model;

public class Vector2 {
    private float x;
    private float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.getX() + other.getX(), this.getY() + other.getY());
    }

    @Override
    public String toString() {
        return "Vector2{" + x +
                ", " + y +
                '}';
    }
}
