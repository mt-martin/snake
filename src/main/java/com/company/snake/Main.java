package com.company.snake;

import java.util.LinkedList;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

public class Main extends Application {

	private static final int WINDOW_SIZE = 500;
	private static final int TILE_SIZE = 20;
	private static final double MOVE_SPEED_SECONDS = 0.1;

	private static Circle food;
	private Rectangle snakeHead;
	private Rectangle snakeBodySegment;

	private static final LinkedList<Rectangle> snakeSegments = new LinkedList<>();
	private static final LinkedList<Pair<Double, Double>> snakeSegmentPositions = new LinkedList<>();
	private final LinkedList<Pair<Double, Double>> snakePositionHistory = new LinkedList<>();

	private boolean canMoveUp = true;
	private boolean canMoveDown = true;
	private boolean canMoveRight = true;
	private boolean canMoveLeft = false;

	private enum Direction {
		UP,
		DOWN,
		RIGHT,
		LEFT
	}

	private Direction currentDirection = Direction.RIGHT;

	@Override
	public void start(Stage stage) throws Exception {
		GridPane gridPane = new GridPane();
		Timeline timeline = new Timeline();
		Scene scene = new Scene(gridPane, WINDOW_SIZE, WINDOW_SIZE, Color.BLACK);

		snakeHead = new Rectangle(TILE_SIZE, TILE_SIZE);
		snakeHead.setFill(Color.LIMEGREEN);
		snakeSegments.add(snakeHead);

		snakeBodySegment = new Rectangle(TILE_SIZE, TILE_SIZE);

		spawnFood(gridPane);

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.UP && canMoveUp) {
					currentDirection = Direction.UP;
					setMovementBooleans(false, false, true, true);
				} else if (keyEvent.getCode() == KeyCode.DOWN && canMoveDown) {
					currentDirection = Direction.DOWN;
					setMovementBooleans(false, false, true, true);
				} else if (keyEvent.getCode() == KeyCode.RIGHT && canMoveRight) {
					currentDirection = Direction.RIGHT;
					setMovementBooleans(true, true, false, false);
				} else if (keyEvent.getCode() == KeyCode.LEFT && canMoveLeft) {
					currentDirection = Direction.LEFT;
					setMovementBooleans(true, true, false, false);
				}
			}
		});

		KeyFrame keyFrame = new KeyFrame(Duration.seconds(MOVE_SPEED_SECONDS), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				switch (currentDirection) {
					case UP -> {
						if (snakeHead.getTranslateY() <= 0) {
							snakeHead.setTranslateY(WINDOW_SIZE - TILE_SIZE);
						} else {
							snakeHead.setTranslateY(snakeHead.getTranslateY() - TILE_SIZE);
						}
					}
					case DOWN -> {
						if (snakeHead.getTranslateY() >= WINDOW_SIZE - TILE_SIZE) {
							snakeHead.setTranslateY(0);
						} else {
							snakeHead.setTranslateY(snakeHead.getTranslateY() + TILE_SIZE);
						}
					}
					case RIGHT -> {
						if (snakeHead.getTranslateX() >= WINDOW_SIZE - TILE_SIZE) {
							snakeHead.setTranslateX(0);
						} else {
							snakeHead.setTranslateX(snakeHead.getTranslateX() + TILE_SIZE);
						}
					}
					case LEFT -> {
						if (snakeHead.getTranslateX() <= 0) {
							snakeHead.setTranslateX(WINDOW_SIZE - TILE_SIZE);
						} else {
							snakeHead.setTranslateX(snakeHead.getTranslateX() - TILE_SIZE);
						}
					}
					default -> throw new IllegalStateException("Unexpected value: " + currentDirection);
				}

				snakePositionHistory.addFirst(new Pair<>(snakeHead.getTranslateX(), snakeHead.getTranslateY()));

				if (snakeHead.getBoundsInParent().intersects(food.getBoundsInParent())) {
					gridPane.getChildren().remove(food);
					spawnFood(gridPane);

					snakeBodySegment = new Rectangle(TILE_SIZE, TILE_SIZE);
					snakeBodySegment.setTranslateX(snakePositionHistory.get(snakeSegments.size()).getKey());
					snakeBodySegment.setTranslateY(snakePositionHistory.get(snakeSegments.size()).getValue());
					snakeBodySegment.setFill(Color.LIGHTGREEN);
					snakeSegments.add(snakeBodySegment);

					gridPane.getChildren().add(snakeBodySegment);
				}

				if (snakeBodySegment != null) {
					for (int i = 0; i < snakeSegments.size(); i++) {
						snakeSegments.get(i).setTranslateX(snakePositionHistory.get(i).getKey());
						snakeSegments.get(i).setTranslateY(snakePositionHistory.get(i).getValue());
					}
				}

				for (Rectangle segment : snakeSegments) {
					snakeSegmentPositions.add(new Pair<>(segment.getTranslateX(), segment.getTranslateY()));
				}
				snakeSegmentPositions.remove(0);

				for (Pair<Double, Double> position : snakeSegmentPositions) {
					double snakeHeadPositionX = snakeHead.getTranslateX();
					double snakeHeadPositionY = snakeHead.getTranslateY();

					if (snakeHeadPositionX == position.getKey() && snakeHeadPositionY == position.getValue()) {
						int segmentIndex = 0;

						snakeSegmentPositions.clear();
						for (Rectangle segment : snakeSegments) {
							if (segmentIndex != 0) {
								gridPane.getChildren().remove(segment);
							}
							segmentIndex++;
						}

						snakeSegments.clear();
						snakeSegments.add(snakeHead);
					}
				}

				snakeSegmentPositions.clear();
			}
		});

		gridPane.getChildren().addAll(snakeHead);

		timeline.getKeyFrames().add(keyFrame);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.playFromStart();

		stage.setResizable(false);
		stage.setTitle("Snake");
		stage.setScene(scene);
		stage.show();
	}

	public void spawnFood(GridPane gridPane) {
		int foodPositionX;
		int foodPositionY;
		boolean isFoodPositionValid = true;

		do {
			foodPositionX = new Random().nextInt(WINDOW_SIZE);
			foodPositionY = new Random().nextInt(WINDOW_SIZE);
		} while ((((foodPositionX % TILE_SIZE) != 0) || ((foodPositionY % TILE_SIZE) != 0)));

		food = new Circle((TILE_SIZE / 2.0) - 1);
		food.setFill(Color.ALICEBLUE);
		food.setTranslateX(foodPositionX + 1);
		food.setTranslateY(foodPositionY);

		for (Rectangle segment : snakeSegments) {
			if (segment.getBoundsInParent().intersects(food.getBoundsInParent())) {
				isFoodPositionValid = false;
				spawnFood(gridPane);
			}
		}

		if (isFoodPositionValid) {
			gridPane.getChildren().add(food);
		}
	}

	private void setMovementBooleans(boolean canMoveUp, boolean canMoveDown, boolean canMoveRight, boolean canMoveLeft) {
		this.canMoveUp = canMoveUp;
		this.canMoveDown = canMoveDown;
		this.canMoveRight = canMoveRight;
		this.canMoveLeft = canMoveLeft;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
