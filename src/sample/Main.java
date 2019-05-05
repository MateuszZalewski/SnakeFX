package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class Main extends Application {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;
    }

    private final Random random = new Random();
    public final static int MS_PER_FRAME = 80;
    public final static int WIDTH = 600;
    public final static int HEIGHT = 300;
    public final static int BLOCK_SIZE = 10;
    public Direction direction = Direction.RIGHT;
    private boolean running = true;
    private boolean moved = false;
    public static final Group root = new Group();
    public Timeline timeline = new Timeline();
    ParallelTransition pt = new ParallelTransition();
    ObservableList<Node> snake = root.getChildren();
    Scene scene = new Scene(root, WIDTH, HEIGHT, Color.WHITE);

    @Override
    public void start(Stage primaryStage) throws Exception {

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        canvas.setFocusTraversable(true);
        scene.setOnKeyPressed(keyEvent -> {
            if (moved) {
                switch (keyEvent.getCode()) {
                    case UP:
                    case W:
                        if (direction != Direction.DOWN) {
                            direction = Direction.UP;
                        }
                        break;
                    case DOWN:
                    case S:
                        if (direction != Direction.UP) {
                            direction = Direction.DOWN;
                        }
                        break;
                    case LEFT:
                    case A:
                        if (direction != Direction.RIGHT) {
                            direction = Direction.LEFT;
                        }
                        break;
                    case RIGHT:
                    case D:
                        if (direction != Direction.LEFT) {
                            direction = Direction.RIGHT;
                        }
                        break;
                }
                moved = false;
            }
        });
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.WHITE);
        root.getChildren().add(canvas);
        Rectangle food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
        food.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        root.getChildren().add(food);
        Rectangle rectangle = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
        rectangle.setTranslateX(0);
        rectangle.setTranslateY(0);
        root.getChildren().add(rectangle);
        TranslateTransition headTrans = new TranslateTransition(Duration.millis(MS_PER_FRAME * 3 / 4), snake.get(2));
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().add(headTrans);
        Rectangle head = (Rectangle) snake.get(2);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(MS_PER_FRAME), e -> {

            if (!running) {
                return;
            }
            System.out.println("(" + head.getTranslateX() + "," + head.getTranslateY() + ")");
            double lastToX = head.getTranslateX() / BLOCK_SIZE * BLOCK_SIZE;
            double lastToY = head.getTranslateY() / BLOCK_SIZE * BLOCK_SIZE;
            switch (direction) {
                case UP:
                    ((TranslateTransition) (pt.getChildren().get(0))).setToX(lastToX);
                    ((TranslateTransition) (pt.getChildren().get(0))).setToY(lastToY - BLOCK_SIZE);
                    break;
                case DOWN:
                    ((TranslateTransition) (pt.getChildren().get(0))).setToX(lastToX);
                    ((TranslateTransition) (pt.getChildren().get(0))).setToY(lastToY + BLOCK_SIZE);
                    break;
                case RIGHT:
                    ((TranslateTransition) (pt.getChildren().get(0))).setToX(lastToX + BLOCK_SIZE);
                    ((TranslateTransition) (pt.getChildren().get(0))).setToY(lastToY);
                    break;
                case LEFT:
                    ((TranslateTransition) (pt.getChildren().get(0))).setToX(lastToX - BLOCK_SIZE);
                    ((TranslateTransition) (pt.getChildren().get(0))).setToY(lastToY);
                    break;
            }
            moved = true;
            for (Animation rectMove : pt.getChildren().subList(1, pt.getChildren().size())) {
                double curToX = ((TranslateTransition) (rectMove)).getToX();
                double curToY = ((TranslateTransition) (rectMove)).getToY();
                ((TranslateTransition) (rectMove)).setToY(lastToY);
                ((TranslateTransition) (rectMove)).setToX(lastToX);
                lastToX = curToX;
                lastToY = curToY;
            }

            for (Node rect : snake.subList(2, snake.size())) {
                if (snake.size() % 2 == 0)
                    ((Rectangle) rect).setFill(Color.BLACK);
                else
                    ((Rectangle) rect).setFill(Color.RED);
                if (head != rect && rect.getTranslateX() == head.getTranslateX() && rect.getTranslateY() == head.getTranslateY()) {
                    stopGame();
                }
            }

            if (head.getTranslateX() == food.getTranslateX() && head.getTranslateY() == food.getTranslateY()) {
                pt.stop();
                Rectangle rect = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
                rect.setTranslateX(lastToX);
                rect.setTranslateY(lastToY);
                if (snake.size() % 2 == 1)
                    rect.setFill(Color.RED);
                else
                    rect.setFill(Color.BLACK);
                root.getChildren().add(rect);
                TranslateTransition rectTT = new TranslateTransition(Duration.millis(MS_PER_FRAME * 3 / 4), rect);
                pt.getChildren().add(rectTT);
                food.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
                food.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
            }
            pt.play();
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.show();
        timeline.play();
    }


    public void stopGame() {
        running = false;
        System.out.println("KUPA");
        timeline.stop();
        snake.clear();
        pt.getChildren().clear();
        System.out.println("kupa");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
