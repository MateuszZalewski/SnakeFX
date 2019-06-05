package sample;

import com.google.gson.Gson;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Main extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("highscore.json"));
            Gson gson = new Gson();
            Highscore highscoreObj = gson.fromJson(br, Highscore.class);
            highscore = highscoreObj.getHighscore();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int score = 0;
    private int highscore = 0;
    private final Random random = new Random();
    private final static int MS_PER_FRAME = 80;
    private final static int WIDTH = 600;
    private final static int HEIGHT = 300;
    private final static int BLOCK_SIZE = 10;
    private Direction direction = Direction.RIGHT;
    private Rectangle head;
    private boolean moved = false;
    private final Pane pane = new Pane();
    private final Label scoreBoard = new Label("score:");
    private Rectangle food;
    private StringProperty valueProperty = new SimpleStringProperty("score: 0");
    private static final Group root = new Group();
    private static long lastRate = 0;
    private AtomicLong curRateChange = new AtomicLong(0);
    private Timeline timeline = new Timeline();
    private ParallelTransition pt = new ParallelTransition();
    private ObservableList<Node> snake = root.getChildren();
    private Scene gameScene = new Scene(pane, WIDTH, HEIGHT, Color.WHITE);
    private BetterRectangleBuilder headBuilder = new BetterRectangleBuilder(BLOCK_SIZE, BLOCK_SIZE, Color.RED);
    private BetterRectangleBuilder bodyBuilder = new BetterRectangleBuilder(100, 100, BLOCK_SIZE, BLOCK_SIZE, Color.RED);
    private BetterRectangleBuilder foodBuilder = new BetterRectangleBuilder(BLOCK_SIZE, BLOCK_SIZE, Color.BLUE);
    private Scene menuScene;
    private Scene highscoreScene;

    @Override
    public void start(Stage primaryStage) {

        Pane highscoreRoot = new Pane();
        Label highscoreLabel = new Label("Highest score: " + highscore);
        highscoreRoot.getChildren().add(highscoreLabel);
        highscoreScene = new Scene(highscoreRoot, WIDTH, HEIGHT, Color.WHITE);


        VBox menuRoot = new VBox();
        menuRoot.setAlignment(Pos.CENTER);
        Button play = new Button("PLAY");
        play.setOnAction(e -> {
            startGame(primaryStage);
        });
        Button highScore = new Button("HIGH SCORES");
        highScore.setOnAction(e -> {
            primaryStage.setScene(highscoreScene);
            primaryStage.show();
        });
        Button exit = new Button("EXIT");
        exit.setOnAction(e -> {
            Platform.exit();
        });
        menuRoot.getChildren().add(play);
        menuRoot.getChildren().add(highScore);
        menuRoot.getChildren().add(exit);
        menuScene = new Scene(menuRoot, WIDTH, HEIGHT, Color.WHITE);

        pane.getChildren().add(root);
        pane.getChildren().add(scoreBoard);
        primaryStage.setResizable(false);
        gameScene.setOnKeyPressed(keyEvent -> {
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
                        restartGame(primaryStage);
                        break;
                    case P:
                        pauseGame();
                        break;
                    case O:
                        unpauseGame();
                        break;
                }
        });

        scoreBoard.textProperty().bind(valueProperty);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(MS_PER_FRAME), e -> {
            double lastToX = (snake.get(1)).getTranslateX() / BLOCK_SIZE * BLOCK_SIZE;
            double lastToY = (snake.get(1)).getTranslateY() / BLOCK_SIZE * BLOCK_SIZE;
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
                stopGame(primaryStage);
            }

            for (Node rect : snake.subList(1, snake.size())) {
                if (curRateChange.get() < 0)
                    ((Rectangle) rect).setFill(Color.RED);
                else
                    ((Rectangle) rect).setFill(Color.GREEN);
                if (head != rect && rect.getTranslateX() == head.getTranslateX() && rect.getTranslateY() == head.getTranslateY()) {
                    stopGame(primaryStage);
                }
            }

            if (head.getTranslateX() == food.getTranslateX() && head.getTranslateY() == food.getTranslateY()) {
                score++;
                if (score > highscore) {
                    scoreBoard.setTextFill(Color.GREEN);
                }
                valueProperty.setValue("score: " + score);
                if (curRateChange.get() < 0)
                    bodyBuilder.setColor(Color.RED);
                else
                    bodyBuilder.setColor(Color.GREEN);
                bodyBuilder.setTranslationXY(lastToX, lastToY);
                Rectangle rect = bodyBuilder.build();
                root.getChildren().add(rect);
                TranslateTransition rectTT = new TranslateTransition(Duration.millis(MS_PER_FRAME - 20), rect);
                pt.getChildren().add(rectTT);
                food.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
                food.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
            }
            pt.play();
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        primaryStage.setTitle("Snake");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    public void startGame(Stage primaryStage) {
        direction = Direction.RIGHT;
        foodBuilder.setTranslateX(random.nextInt(WIDTH - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        foodBuilder.setTranslateY(random.nextInt(HEIGHT - BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE);
        food = foodBuilder.build();
        snake.clear();
        snake.add(food);
        head = headBuilder.build();
        snake.add(head);
        TranslateTransition headTrans = new TranslateTransition(Duration.millis(MS_PER_FRAME * 3 / 4), head);
        pt.getChildren().add(headTrans);
        primaryStage.setScene(gameScene);
        primaryStage.show();
        pt.play();
        timeline.play();
        moved = false;
    }

    private void stopGame(Stage primaryStage) {
        timeline.stop();
        pt.stop();
        pt.getChildren().clear();
        scoreBoard.setTextFill(Color.BLACK);
        setHighScore(score);
        score = 0;
        valueProperty.setValue("score: 0");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void restartGame(Stage primaryStage) {
        stopGame(primaryStage);
        startGame(primaryStage);
    }

    private void setHighScore(int score) {
        if (score < highscore)
            return;
        String json = new Gson().toJson(new Highscore(score));
        highscore = score;
        try {
            //write converted json data to a file named "CountryGSON.json"
            FileWriter writer = new FileWriter("highscore.json");
            writer.write(json);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseGame() {
        timeline.pause();
        pt.pause();
    }

    private void unpauseGame() {
        pt.play();
        timeline.play();
    }

    private void moveRectangle(Rectangle rect, double x, double y) {
        rect.setTranslateX(x);
        rect.setTranslateY(y);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Task<Integer> task = new Task<>() {
        @Override
        protected Integer call() {
            ForexRates forexRates = new ForexRates();
            while (true) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    break;
                }
                curRateChange.set((long) (forexRates.getRatesChange() * 100000000));
                try {
                    Thread.sleep(5000);
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
