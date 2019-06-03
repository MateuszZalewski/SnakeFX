package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BetterRectangleBuilder {
    private double arcWidth;
    private double arcHeight;
    private double translateX;
    private double translateY;
    private double width;
    private double height;
    private Color color;

    public BetterRectangleBuilder(double arcWidth, double arcHeight, double translateX, double translateY, double width, double height, Color color) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.translateX = translateX;
        this.translateY = translateY;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public BetterRectangleBuilder(double arcWidth, double arcHeight, double width, double height, Color color) {
        this(arcWidth, arcHeight, 0, 0, width, height, color);
    }

    public BetterRectangleBuilder(double width, double height, Color color) {
        this(0, 0, width, height, color);
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
    }

    public void setTranslationXY(double translateX, double translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Rectangle build() {
        Rectangle rectangle = new Rectangle(width, height, color);
        rectangle.setArcWidth(arcWidth);
        rectangle.setArcHeight(arcHeight);
        rectangle.setTranslateX(translateX);
        rectangle.setTranslateY(translateY);
        return rectangle;
    }
}
