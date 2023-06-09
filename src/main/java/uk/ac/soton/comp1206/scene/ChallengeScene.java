package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Multimedia;

import java.util.HashSet;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    protected Game game;

    //holds the current piece
    protected PieceBoard currentPiece;
    //holds the following piece
    protected PieceBoard followingPiece;
    //holds the Game Board
    protected GameBoard board;

    protected ProgressBar progressBar;

    private Timeline timeline;
    private String playerName;


    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    public ChallengeScene(GameWindow gameWindow, String playerName) {
        super(gameWindow);
        this.playerName = playerName;
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add(SettingsScene.theme.getText());
        root.getChildren().add(challengePane);
        root.setMaxWidth(gameWindow.getWidth()*2);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);


        //Add the state panel to the left of the screen
        var statePanel = addStatePanel();
        statePanel.setPadding(new Insets(10, 10, 10, 10));
        mainPane.setLeft(statePanel);


        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(30);

        //displays the current piece in a pieceBoard object
        currentPiece = new PieceBoard(gameWindow.getHeight() / 6.0, gameWindow.getHeight() / 6.0);
        currentPiece.getStyleClass().add("gameBox");
        vBox.getChildren().add(currentPiece);

        //displays the next piece in a pieceBoard object
        followingPiece = new PieceBoard(gameWindow.getHeight() / 11.0, gameWindow.getHeight() / 11.0);
        followingPiece.getStyleClass().add("gameBox");
        vBox.getChildren().add(followingPiece);

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(20);
        progressBar.setProgress(0);
        vBox.getChildren().add(progressBar);

        mainPane.setRight(vBox);


        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
        mainPane.setCenter(board);
        board.getStyleClass().add("gameBox");


        //Handle block on gameBoard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Handle rotation on block when right-clicked
        board.setOnRightClick((gameBlock) -> rightRotate());

        //Handle rotation on block when pieceBoard grid is clicked
        currentPiece.setOnBlockClick((gameBlock) -> rightRotate());
    }

    /**
     * Handle when a block is clicked
     *
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * reset the next currentPiece and next followingPiece on the pieceBoard
     */
    protected void nextPiece(GamePiece nextCurrentPiece, GamePiece nextFollowingPiece) {
        this.currentPiece.setPiece(nextCurrentPiece);
        this.followingPiece.setPiece(nextFollowingPiece);
    }

    /**
     * rightRotate the block
     */
    public void rightRotate() {
        game.rotateCurrentPiece(1);
        currentPiece.setPiece(game.getCurrentPiece());
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * leftRotate the block
     */
    public void leftRotate() {
        game.rotateCurrentPiece(3);
        currentPiece.setPiece(game.getCurrentPiece());
        Multimedia.playAudio("rotate.wav");
    }

    /**
     * swaps the current piece with the following one and reset
     */
    public void swapPiece() {
        game.swapCurrentPiece();
        currentPiece.setPiece(game.getCurrentPiece());
        followingPiece.setPiece(game.getFollowingPiece());
        Multimedia.playAudio("transition.wav");
    }

    /**
     * handles the fade out animation on the board
     *
     * @param blockCoordinates include the blocks to be faded
     */
    public void clearedLine(HashSet<GameBlockCoordinate> blockCoordinates) {
        board.fadeOutLine(blockCoordinates);
    }

    /**
     * Set up the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
        game.setPlayerName(playerName);

    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        //listens to when the next piece is fetched and calls the respective in class method
        this.game.setNextPieceListener(this::nextPiece);
        this.game.setGameOverListener(this::gameOver);

        //listens to when a key is pressed and calls the respective in class method
        scene.setOnKeyPressed(this::keyPressed);

        //listens to when a line is cleared and calls the respective in class method
        game.setLineClearedListener(this::clearedLine);

        //listens to when a game loop starts and calls the respective in class method
        game.setGameLooplistener((delay) -> {
            //update the time label
            timeline = new Timeline(
                    //green->yellow->red
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1), new KeyValue(progressBar.styleProperty(), " -fx-accent: #00ff00;")),
                    new KeyFrame(Duration.millis(delay / 3.0), new KeyValue(progressBar.progressProperty(), 0.66), new KeyValue(progressBar.styleProperty(), " -fx-accent: #ffff00;")),
                    new KeyFrame(Duration.millis(delay / 3.0 * 2), new KeyValue(progressBar.progressProperty(), 0.33), new KeyValue(progressBar.styleProperty(), " -fx-accent: #ff0000")),
                    new KeyFrame(Duration.millis(delay), new KeyValue(progressBar.progressProperty(), 0))
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        });
        game.start();
    }

    private void gameOver(Game game) {
        logger.info("Game Over");
        this.game.stop();
        timeline.stop();
        //show the game over screen,switch to the scores screen
        gameWindow.startScoreBoard(game);
    }


    /**
     * handles the events that occur when a key is pressed
     * this method enables keyboard support
     *
     * @param event have the type of the pressed key
     */
    public void keyPressed(KeyEvent event) {
        switch (event.getCode()) {
            //when escape is pressed the game is stopped and the player is brought
            //back to the menu
            case ESCAPE -> quit();
            //when space or r is pressed the pieces swapPiece
            case SPACE, R -> swapPiece();

            //when enter or x is pressed the block is placed
            case ENTER, X -> blockClicked(board.getHoverBlock());

            //when arrow pressed change the indicator
            case RIGHT, D ,LEFT, A ,UP, W ,DOWN, S -> board.doHover(event.getCode());

            //when the open bracket or q is pressed the block rotates to the left
            case OPEN_BRACKET, Q, Z -> leftRotate();

            //when the close bracket or e is pressed the block rotates to the right
            case CLOSE_BRACKET, E, C -> rightRotate();
        }
    }

    private void quit() {
        //before we quit, we need to reset the game
        gameOver(game);
    }

    /**
     * Add a state panel to the scene
     * Add Ul elements to show the score, level, multiplier and lives in the ChallengeScene bybinding to the game properties.
     * Tip: Use the .asString method on an Integer Property to get it as a bindable string!
     */
    public VBox addStatePanel() {

        var vbox = new VBox();
        var playerText = new Text("Player Name: "+this.playerName);
        playerText.getStyleClass().add("title");
        vbox.getChildren().add(playerText);

        var highestPlayer = new Text();
        highestPlayer.textProperty().bind(Bindings.format("Highest Player: %s", game.highestPlayer));
        highestPlayer.getStyleClass().add("title");
        vbox.getChildren().add(highestPlayer);

        var highestScore = new Text();
        highestScore.textProperty().bind(Bindings.format("Highest Score: %d", game.highestScore));
        highestScore.getStyleClass().add("title");
        vbox.getChildren().add(highestScore);


        var scoreText = new Text();
        scoreText.textProperty().bind(game.score.asString("Score: %d"));
        scoreText.getStyleClass().add("score");
        vbox.getChildren().add(scoreText);

        var livesText = new Text();
        livesText.textProperty().bind(game.lives.asString("Lives: %d"));
        livesText.getStyleClass().add("lives");
        vbox.getChildren().add(livesText);

        var levelText = new Text();
        levelText.textProperty().bind(game.level.asString("Level: %d"));
        levelText.getStyleClass().add("level");
        vbox.getChildren().add(levelText);


        var multiplierText = new Text();
        multiplierText.textProperty().bind(game.multiplier.asString("Multiplier: %d"));
        multiplierText.getStyleClass().add("level");
        vbox.getChildren().add(multiplierText);

        return vbox;
    }


    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
