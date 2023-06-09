package uk.ac.soton.comp1206.utils;

import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;

import java.util.Objects;

/**
 * Multimedia
 * Add two MediaPlayer fields to handle an audio player and music player
 * These need to be fields, not local variables, to avoid them being garbage
 * collected and sound stopping shortly after it starts
 * Add a method to play an audio file
 * Add a method to play background music
 * The background music should loop
 */


public class Multimedia {
    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;

    private static double audioVolume = 1;

    private static double musicVolume = 1;

    private static boolean playing = false;


    private static final Logger logger = LogManager.getLogger(Game.class);

    public static void playAudio(String filePath) {
        try {
            String musicFilePath = Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + filePath)).toExternalForm();
            logger.info("Playing audio file: " + musicFilePath);
            Media audio = new Media(musicFilePath);
            audioPlayer = new MediaPlayer(audio);
            audioPlayer.setVolume(audioVolume);
            audioPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    public static void playMusic(String fileName) {
        try {
            String musicFilePath = Objects.requireNonNull(Multimedia.class.getResource("/music/" + fileName)).toExternalForm();
            logger.info("Playing music file: " + musicFilePath);
            Media music = new Media(musicFilePath);
            musicPlayer = new MediaPlayer(music);
            musicPlayer.setVolume(musicVolume);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
            playing = true;
            //wait here
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play music file");
        }
    }

    //stop music
    public static void stopMusic() {
        if (playing) {
            musicPlayer.stop();
            playing = false;
        }
    }

    public static ImageView getImageView(String fileName) {
        String imageFilePath = Objects.requireNonNull(Multimedia.class.getResource("/images/" + fileName)).toExternalForm();
        logger.info("Loading image file: " + imageFilePath);
        return new ImageView(imageFilePath);
    }

    //Set the music volume
    public static void setMusicVolume(double volume) {
        musicVolume = volume;
        if(musicPlayer!= null)
            musicPlayer.setVolume(musicVolume);
    }

    //Set audio volume
    public static void setAudioVolume(double volume) {
        audioVolume = volume;
        if(audioPlayer!= null)
            audioPlayer.setVolume(audioVolume);
    }
}
