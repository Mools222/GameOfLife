import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * This class represents a single cell in the Game Of Life.
 */

class Cell extends StackPane {
    private int livingNeighbours;
    private boolean alive;

    public Cell(int cellDimensions) {
        setPrefHeight(cellDimensions);
        setPrefWidth(cellDimensions);
//        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        setStyle("-fx-background-color: #DCDCDC; -fx-border-color: #000000; -fx-border-width: 1");

        setOnMouseClicked(event -> spawn());
    }

    /**
     * This method changes the status of the cell to dead or alive in accordance with the number of neighbours of the cell.
     */

    public void update() {
        if (alive)
            alive = livingNeighbours == 2 || livingNeighbours == 3;
        else
            alive = livingNeighbours == 3;
    }

    /**
     * This method creates a visual presentation of the cell, if the cell is alive.
     */

    public void draw() {
        getChildren().clear();
        if (alive)
            getChildren().add(new Circle(getHeight() / 3, Color.color(Math.random(), Math.random(), Math.random(), 1)));
    }

    /**
     * This method gives a cell life and creates a visual presentation of the cell.
     */

    public void spawn() {
        if (!alive) {
            alive = true;
            getChildren().add(new Circle(getHeight() / 3, Color.color(Math.random(), Math.random(), Math.random(), 1)));
        } else {
            alive = false;
            getChildren().clear();
        }
    }

    /**
     * This method kills the cell and removes any visual representation.
     */

    public void clear() {
        alive = false;
        getChildren().clear();
    }

    public int getLivingNeighbours() {
        return livingNeighbours;
    }

    public void setLivingNeighbours(int livingNeighbours) {
        this.livingNeighbours = livingNeighbours;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}