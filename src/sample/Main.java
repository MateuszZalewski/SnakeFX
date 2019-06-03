package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Main extends Application {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int score = 0;

    private final Random random = new Random();
    private final static int MS_PER_FRAME = 80;
    private final static int WIDTH = 600;
    private final static int HEIGHT = 300;
    private final static int BLOCK_SIZE = 10;
    private Direction direction = Direction.RIGHT;
    private Rectangle head;
    private boolean running = true;
    private boolean moved = false;
    private final Pane pane = new Pane();
    private final Label scoreBoard = new Label("score:");
    private Rectangle food;
    StringProperty valueProperty = new SimpleStringProperty("score: 0");
    private static final Group root = new Group();
    private static long lastRate = 0;
    AtomicLong curRateChange = new AtomicLong(0);
    private Timeline timeline = new Timeline();
    private ParallelTransition pt = new ParallelTransition();
    private ObservableList<Node> snake = root.getChildren();
    private Scene scene = new Scene(pane, WIDTH, HEIGHT, Color.WHITE);


    @Override

    public void start(Stage primaryStage) {
        pane.getChildren().add(root);
        pane.getChildren().add(scoreBoard);
        primaryStage.setResizable(false);
        scene.setOnKeyPressed(keyEvent -> {
                switch (keyEvent.getCode()) {
                    case UP:
                    case W:
                        if (direction != Direction.DOWN && moved) {
                            direction = Direction.UP;
                            moved = false;
                        }
                        break;
                    case DOWN:
                    case S:
                        if (direction != Direction.UP && moved) {
                            direction = Direction.DOWN;
                            moved = false;
                        }
                        break;
                    case LEFT:
                    case A:
                        if (direction != Direction.RIGHT && moved) {
                            direction = Direction.LEFT;
                            moved = false;
                        }
                        break;
                    case RIGHT:
                    case D:
                        if (direction != Direction.LEFT && moved) {
                            direction = Direction.RIGHT;
                            moved = false;
                        }
                        break;
                    case SPACE:
                    case R:
                        restartGame();
                        break;
                    case P:
                        pauseGame();
                        break;
                    case O:
                        unpauseGame();
                        break;
                }
        });

        startGame();
        head = (Rectangle) snake.get(1);
        scoreBoard.textProperty().bind(valueProperty);
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        long time = System.currentTimeMillis();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(MS_PER_FRAME), e -> {
            if (running) {
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


                if (head.getTranslateX() < 0 || head.getTranslateY() < 0 || head.getTranslateX() >= WIDTH || head.getTranslateY() >= HEIGHT) {
                    restartGame();
                }

                for (Node rect : snake.subList(1, snake.size())) {
                    if (curRateChange.get() < 0)
                        ((Rectangle) rect).setFill(Color.RED);
                    else
                        ((Rectangle) rect).setFill(Color.GREEN);
                    if (head != rect && rect.getTranslateX() == head.getTranslateX() && rect.getTranslateY() == head.getTranslateY()) {
                        restartGame();
                    }
                }

                if (head.getTranslateX() == food.getTranslateX() && head.getTranslateY() == food.getTranslateY()) {
//                pt.stop();
                    score++;
                    valueProperty.setValue("score: " + score);
                    Rectangle rect = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
                    rect.setTranslateX(lastToX);
                    rect.setTranslateY(lastToY);
                    rect.setArcWidth(1000);
                    rect.setArcHeight(1000);
                    if (curRateChange.get() < 0)
                        rect.setFill(Color.RED);
                    else
                        rect.setFill(Color.GREEN);
                    root.getChildren().add(rect);
                    TranslateTransition rectTT = new TranslateTransition(Duration.millis(MS_PER_FRAME - 20), rect);
                    pt.getChildren().add(rectTT);
                    food.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
                    food.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
                }
                pt.play();
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.show();
        timeline.play();
    }

    public void startGame() {
        direction = Direction.RIGHT;
        food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
        food.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        snake.clear();
        snake.add(food);
        head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
        head.setTranslateX(0);
        head.setTranslateY(0);
        snake.add(head);
        TranslateTransition headTrans = new TranslateTransition(Duration.millis(MS_PER_FRAME * 3 / 4), head);
        pt.getChildren().add(headTrans);
        pt.play();
        timeline.play();
        running = true;
        moved = false;
    }

    public void stopGame() {
        running = false;
        timeline.stop();
        pt.stop();
        pt.getChildren().clear();
        score = 0;
        valueProperty.setValue("score: 0");
    }

    public void restartGame() {
        stopGame();
        startGame();
    }

    public void pauseGame() {
        running = false;
    }

    public void unpauseGame() {
        running = true;
    }

    public static void main(String[] args) {
        launch(args);
    }

    Task<Integer> task = new Task<Integer>() {
        @Override
        protected Integer call() throws Exception {
            int iterations;
            ForexRates forexRates = new ForexRates();
            while (true) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                curRateChange.set((long) (forexRates.getRatesChange() * 100000000));
                //Block the thread for a short time, but be sure
                //to check the InterruptedException for cancellation
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interrupted) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        break;
                    }
                }
            }
            return 1;
        }
    };

}
