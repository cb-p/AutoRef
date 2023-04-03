package nl.roboteamtwente.autoref.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nl.roboteamtwente.autoref.SSLAutoRef;
import nl.roboteamtwente.autoref.model.FieldLine;
import nl.roboteamtwente.autoref.model.Robot;
import nl.roboteamtwente.autoref.model.TeamColor;
import nl.roboteamtwente.autoref.model.Vector2;

public class GameCanvas extends Canvas {
    private SSLAutoRef sslAutoRef;

    public void setSslAutoRef(SSLAutoRef sslAutoRef) {
        this.sslAutoRef = sslAutoRef;
    }

    @Override
    public double minHeight(double width) {
        return 64;
    }

    @Override
    public double maxHeight(double width) {
        return 1000;
    }

    @Override
    public double prefHeight(double width) {
        return minHeight(width);
    }

    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double maxWidth(double height) {
        return 10000;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
        redraw();
    }

    public void redraw() {
        GraphicsContext g = getGraphicsContext2D();

        if (sslAutoRef.getReferee().getGame() == null) {
            return;
        }

        float scale = (float) Math.min(
                getWidth() / (sslAutoRef.getReferee().getGame().getField().getSize().getX() + 0.8f),
                getHeight() / (sslAutoRef.getReferee().getGame().getField().getSize().getY() + 0.5f)
        );
        ScaledDrawer s = new ScaledDrawer(g, new Vector2(0, 0), scale);

        g.setFill(Color.GREEN);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFill(Color.WHITE);
        g.fillText(sslAutoRef.getReferee().getGame().getState().toString(), 10.0, 20.0);

        for (FieldLine fieldLine : sslAutoRef.getReferee().getGame().getField().getLines()) {
            s.drawLine(fieldLine.p1(), fieldLine.p2(), 2, Color.WHITE);

//            g.setFill(Color.WHITE);
//            g.fillText(
//                    fieldLine.name(),
//                    s.translateX((fieldLine.p1().getX() + fieldLine.p2().getX()) / 2),
//                    s.translateY((fieldLine.p1().getY() + fieldLine.p2().getY()) / 2));
        }

        for (Robot robot : sslAutoRef.getReferee().getGame().getRobots()) {
            s.drawCircle(robot.getPosition().xy(), 0.2f, robot.getTeam().getColor() == TeamColor.BLUE ? Color.BLUE : Color.YELLOW);
            s.drawCircle(robot.getPosition().xy(), 0.1f, robot.isGoalkeeper() ? Color.BLACK : Color.WHITE);
            s.drawLine(robot.getPosition().xy(), robot.getPosition().xy().add(new Vector2(0.15f, 0.0f).rotate(robot.getAngle())), 2, Color.CYAN);

            g.setFill(Color.WHITE);
            float x = s.translateX(robot.getPosition().getX());
            float y = s.translateY(robot.getPosition().getY());
            g.fillText(robot.getTeam().getColor() + " " + robot.getId(), x, y - 20f);
        }

        s.drawCircle(sslAutoRef.getReferee().getGame().getBall().getPosition().xy(), 0.15f, Color.RED);
        s.drawLine(sslAutoRef.getReferee().getGame().getBall().getPosition().xy(),
                sslAutoRef.getReferee().getGame().getBall().getPosition().xy().add(sslAutoRef.getReferee().getGame().getBall().getVelocity().xy()), 2f, Color.ORANGE);
    }
}
