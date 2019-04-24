import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;

/**
 * @author https://github.com/Mools222/
 * The Game Of Life program is a cellular automaton devised by the British mathematician John Horton Conway in 1970.
 * https://docs.google.com/document/d/1NFB-1S7oyIsEiSNahkTgNjsTl9kb8nTDQo0grbtTtBI/edit
 */

public class Game extends Application {
    private SimpleIntegerProperty cycleCounter = new SimpleIntegerProperty(0);
    private Timeline timeline;
    private Button buttonPlay;
    private Stage primaryStage;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);

        final int DIMENSIONS = 900;
        BoardPane boardPane = new BoardPane(DIMENSIONS, 20);

        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> boardPane.cycle()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        Button buttonNext = new Button("Next");
        buttonPlay = new Button("Play");
        Button buttonClear = new Button("Clear");

        buttonNext.setOnAction(e -> {
            stopAnimation();
            boardPane.cycle();
        });

        buttonPlay.setOnAction(e -> {
            if (buttonPlay.getText().equals("Play")) {
                timeline.play();
                buttonPlay.setText("Stop");
            } else {
                timeline.stop();
                buttonPlay.setText("Play");
            }
        });

        buttonClear.setOnAction(e -> {
            stopAnimation();
            boardPane.clear();
        });

        ColorPicker colorPicker = new ColorPicker(Color.GAINSBORO);
        colorPicker.setOnAction(event -> boardPane.changeBackgroundColor(colorPicker.getValue()));

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Random", "Glider", "Small exploder", "Exploder", "10 cell row", "Lightweight spaceship", "Tumbler", "Gospel glider gun");

        comboBox.setOnAction(e -> {
            stopAnimation();
            boardPane.clear();
            boardPane.premadePatterns(comboBox.getValue());
        });

        RadioButton radioButton1 = new RadioButton("10"), radioButton2 = new RadioButton("20"), radioButton3 = new RadioButton("30"), radioButton4 = new RadioButton("50"), radioButton5 = new RadioButton("60"), radioButton6 = new RadioButton("90");
        radioButton2.setSelected(true);

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(radioButton1, radioButton2, radioButton3, radioButton4, radioButton5, radioButton6);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            stopAnimation();
            boardPane.redrawBoard(DIMENSIONS, Integer.parseInt(newValue.toString().substring(newValue.toString().length() - 3, newValue.toString().length() - 1)));
        });

        Slider slider = new Slider(0, 10, 1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setPrefWidth(500);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        timeline.rateProperty().bind(slider.valueProperty());

        Text textCycle = new Text();
        textCycle.textProperty().bind(cycleCounter.asString());

        HBox hBox1 = new HBox(10);
        hBox1.getChildren().addAll(new Label("Controls:"), buttonNext, buttonPlay, buttonClear, new Label("Insert pre-made pattern:"), comboBox);
        hBox1.setAlignment(Pos.CENTER);

        HBox hBox2 = new HBox(10);
        hBox2.getChildren().addAll(new Label("Number of rows and columns:"), radioButton1, radioButton2, radioButton3, radioButton4, radioButton5, radioButton6, new Label("Background color:"), colorPicker);
        hBox2.setAlignment(Pos.CENTER);

        HBox hBox3 = new HBox(10);
        hBox3.getChildren().addAll(new Label("Animation rate:"), slider, new Label("Cycle:"), textCycle);
        hBox3.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(5);
        vBox.getChildren().addAll(boardPane, hBox1, hBox2, hBox3);

        Scene scene = new Scene(vBox);
        primaryStage.setTitle("Game Of Life by Mools"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
    }

    private void stopAnimation() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.stop();
            buttonPlay.setText("Play");
        }
    }

    /**
     * This class holds the board and every cell of the Game Of Life.
     */

    class BoardPane extends GridPane {
        private int cellDimensions, rowsAndColumns;
        private Cell[][] cells;

        BoardPane(int dimensions, int rowsAndColumns) {
            cellDimensions = dimensions / rowsAndColumns;
            this.rowsAndColumns = rowsAndColumns;
            cells = new Cell[rowsAndColumns][rowsAndColumns];
            drawBoard();
        }

        /**
         * This method draws the board the game is played on. It also adds each cell to a two-dimensional array.
         */

        private void drawBoard() {
            getChildren().clear();
            cycleCounter.set(0);

            for (int i = 0; i < rowsAndColumns; i++) {
                for (int j = 0; j < rowsAndColumns; j++) {
                    Cell cell = new Cell(cellDimensions);
                    addRow(i, cell);
                    cells[i][j] = cell;
                }
            }
        }

        /**
         * This method re-draws the board when new dimensions or a new number of rows and columns is given.
         */

        void redrawBoard(int dimensions, int rowsAndColumns) {
            cellDimensions = dimensions / rowsAndColumns;
            this.rowsAndColumns = rowsAndColumns;
            cells = new Cell[rowsAndColumns][rowsAndColumns];
            drawBoard();
        }

        /**
         * This method completes one cycle of the program and increments the cycle counter.
         */

        void cycle() {
            countNeighbours();
            update();
            draw();
            cycleCounter.set(cycleCounter.get() + 1);
        }

        /**
         * This method counts the number of live neighbours each cell has and adds it to the memory of the cell.
         */

        void countNeighbours() {
            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells.length; j++) {
                    cells[i][j].setLivingNeighbours(0);
                    if (i >= 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i - 1][j].isAlive() ? 1 : 0)); // N
                    if (i >= 1 && j < cells.length - 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i - 1][j + 1].isAlive() ? 1 : 0)); // NE
                    if (j < cells.length - 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i][j + 1].isAlive() ? 1 : 0)); // E
                    if (i < cells.length - 1 && j < cells.length - 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i + 1][j + 1].isAlive() ? 1 : 0)); // SE
                    if (i < cells.length - 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i + 1][j].isAlive() ? 1 : 0)); // S
                    if (i < cells.length - 1 && j >= 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i + 1][j - 1].isAlive() ? 1 : 0)); // SW
                    if (j >= 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i][j - 1].isAlive() ? 1 : 0)); // W
                    if (i >= 1 && j >= 1)
                        cells[i][j].setLivingNeighbours(cells[i][j].getLivingNeighbours() + (cells[i - 1][j - 1].isAlive() ? 1 : 0)); // NW
                }
        }

        /**
         * This method calls the update() method for each {@link Cell}.
         */

        void update() {
            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells.length; j++)
                    cells[i][j].update();
        }

        /**
         * This method calls the draw() method for each {@link Cell}.
         */

        void draw() {
            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells.length; j++)
                    cells[i][j].draw();
        }

        /**
         * This method calls the clear() method for each {@link Cell}.
         */

        void clear() {
            cycleCounter.set(0);

            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells.length; j++)
                    cells[i][j].clear();
        }

        /**
         * This method changes the background color of each {@link Cell}.
         */

        void changeBackgroundColor(Color color) {
            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells.length; j++)
                    cells[i][j].setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        /**
         * This method creates pre-made patterns on the board.
         * If the board is too small, a "Toast" message is displayed.
         */

        void premadePatterns(String pattern) {
            int middle = rowsAndColumns / 2;

            switch (pattern) {
                case "Random":
                    Random random = new Random();
                    for (int i = 0; i < cells.length; i++)
                        for (int j = 0; j < cells.length; j++)
                            if (random.nextInt(8) == 0) // Each cell has a 1/8 chance to be made alive.
                                cells[i][j].spawn();
                    break;
                case "Glider":
                    cells[0][1].spawn();
                    cells[1][2].spawn();
                    cells[2][0].spawn();
                    cells[2][1].spawn();
                    cells[2][2].spawn();
                    break;
                case "Small exploder":
                    cells[middle - 2][middle].spawn();
                    cells[middle - 1][middle - 1].spawn();
                    cells[middle - 1][middle].spawn();
                    cells[middle - 1][middle + 1].spawn();
                    cells[middle][middle - 1].spawn();
                    cells[middle][middle + 1].spawn();
                    cells[middle + 1][middle].spawn();
                    break;
                case "Exploder":
                    cells[middle - 2][middle - 2].spawn();
                    cells[middle - 2][middle].spawn();
                    cells[middle - 2][middle + 2].spawn();
                    cells[middle - 1][middle - 2].spawn();
                    cells[middle - 1][middle + 2].spawn();
                    cells[middle][middle - 2].spawn();
                    cells[middle][middle + 2].spawn();
                    cells[middle + 1][middle - 2].spawn();
                    cells[middle + 1][middle + 2].spawn();
                    cells[middle + 2][middle - 2].spawn();
                    cells[middle + 2][middle].spawn();
                    cells[middle + 2][middle + 2].spawn();
                    break;
                case "10 cell row":
                    for (int i = -5; i < 5; i++)
                        cells[middle][middle + i].spawn();
                    break;
                case "Lightweight spaceship":
                    for (int i = 1; i < 5; i++)
                        cells[middle][i].spawn();
                    cells[middle + 1][0].spawn();
                    cells[middle + 1][4].spawn();
                    cells[middle + 2][4].spawn();
                    cells[middle + 3][0].spawn();
                    cells[middle + 3][3].spawn();
                    break;
                case "Tumbler":
                    cells[middle - 2][middle - 2].spawn();
                    cells[middle - 2][middle - 1].spawn();
                    cells[middle - 2][middle + 1].spawn();
                    cells[middle - 2][middle + 2].spawn();
                    cells[middle][middle - 1].spawn();
                    cells[middle][middle + 1].spawn();
                    cells[middle + 1][middle - 4].spawn();
                    cells[middle + 1][middle - 3].spawn();
                    cells[middle + 1][middle - 1].spawn();
                    cells[middle + 1][middle + 1].spawn();
                    cells[middle + 1][middle + 3].spawn();
                    cells[middle + 1][middle + 4].spawn();
                    cells[middle + 2][middle - 4].spawn();
                    cells[middle + 2][middle - 3].spawn();
                    cells[middle + 2][middle - 2].spawn();
                    cells[middle + 2][middle + 2].spawn();
                    cells[middle + 2][middle + 3].spawn();
                    cells[middle + 2][middle + 4].spawn();
                    break;
                case "Gospel glider gun":
                    if (rowsAndColumns >= 50) {
                        cells[middle - 5][middle + 5].spawn();
                        cells[middle - 5][middle + 6].spawn();
                        cells[middle - 5][middle + 16].spawn();
                        cells[middle - 5][middle + 17].spawn();
                        cells[middle - 4][middle + 4].spawn();
                        cells[middle - 4][middle + 6].spawn();
                        cells[middle - 4][middle + 16].spawn();
                        cells[middle - 4][middle + 17].spawn();
                        cells[middle - 3][middle - 18].spawn();
                        cells[middle - 3][middle - 17].spawn();
                        cells[middle - 3][middle - 9].spawn();
                        cells[middle - 3][middle - 8].spawn();
                        cells[middle - 3][middle + 4].spawn();
                        cells[middle - 3][middle + 5].spawn();
                        cells[middle - 2][middle - 18].spawn();
                        cells[middle - 2][middle - 17].spawn();
                        cells[middle - 2][middle - 10].spawn();
                        cells[middle - 2][middle - 8].spawn();
                        cells[middle - 1][middle - 10].spawn();
                        cells[middle - 1][middle - 9].spawn();
                        cells[middle - 1][middle - 2].spawn();
                        cells[middle - 1][middle - 1].spawn();
                        cells[middle][middle - 2].spawn();
                        cells[middle][middle].spawn();
                        cells[middle + 1][middle - 2].spawn();
                        cells[middle + 2][middle + 17].spawn();
                        cells[middle + 2][middle + 18].spawn();
                        cells[middle + 3][middle + 17].spawn();
                        cells[middle + 3][middle + 19].spawn();
                        cells[middle + 4][middle + 17].spawn();
                        cells[middle + 7][middle + 6].spawn();
                        cells[middle + 7][middle + 7].spawn();
                        cells[middle + 7][middle + 8].spawn();
                        cells[middle + 8][middle + 6].spawn();
                        cells[middle + 9][middle + 7].spawn();
                    } else {
                        System.out.println("The number of rows and columns must be at least 50.");

                        Stage stage = new Stage(StageStyle.TRANSPARENT); // Creates a new stage to show a an Android Toast-like message
                        stage.setX(primaryStage.getX() + primaryStage.getWidth() / 4);
                        stage.setY(primaryStage.getY() + primaryStage.getHeight() / 4);
                        stage.setAlwaysOnTop(true);
                        stage.setResizable(false);
                        Label labelToast = new Label("The number of rows and\ncolumns must be at least 50.");
                        labelToast.setStyle("-fx-font-size: 35; -fx-padding: 10; -fx-background-color: white; -fx-background-radius: 20; -fx-border-color: black; -fx-border-radius: 20; -fx-border-width: 2");
                        labelToast.setOnMouseClicked(event -> stage.close()); // Clicking the label closes the Toast
                        Scene scene = new Scene(labelToast, Color.TRANSPARENT);
                        stage.setScene(scene);
                        stage.show();
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000); // Automatically close the Toast after 3 seconds
                                Platform.runLater(() -> stage.close());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                    break;
            }
        }
    }
}

