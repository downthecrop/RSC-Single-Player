package org.nemotech.rsc.client.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import org.nemotech.rsc.client.mudclient;
import org.nemotech.rsc.Constants;
import org.nemotech.rsc.util.Util;

public class MusicPlayer {
    
    private Sequencer sequencer;
    private String currentSong;
    private boolean paused = false;

    private boolean debug = true;
    
    public MusicPlayer() {
        if(debug){
            return;
        }
        try {
            /* obtains the default sequencer connected to a default device */
            sequencer = MidiSystem.getSequencer();
            /* opens the device */
            sequencer.open();
            sequencer.addMetaEventListener((MetaMessage event) -> {
                /* done playing */
                if(event.getType() == 47) {
                    if(mudclient.getInstance().optionMusicLoop) {
                        startRandom();
                    } else {
                        currentSong = null;
                        mudclient.getInstance().selectedSong = null;
                    }
                }
            });
        } catch(MidiUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    public void pause() {
        sequencer.stop();
        paused = true;
    }
    
    public void resume() {
        sequencer.start();
        paused = false;
    }
    
    public String getCurrentSong() {
        if(debug){
            return "return";
        }
        return currentSong;
    }
    
    public void startRandom() {
        File directory = new File(Constants.CACHE_DIRECTORY + "audio" + File.separator + "music" + File.separator);
        FilenameFilter filter = (File dir, String name) -> {
            return name.endsWith(".mid");
        };
        File[] musicFiles = directory.listFiles(filter);
        String fileName = musicFiles[Util.random(musicFiles.length - 1)].getName();
        start(fileName);
    }
    
    public void start(String fileName) {
        if(debug){
            return;
        }
        if(!sequencer.isOpen()) {
            System.out.println("Sequencer not open");
            return;
        }
        new Thread(() -> { 
            try {
                File directory = new File(Constants.CACHE_DIRECTORY + "audio" + File.separator + "music" + File.separator);
                InputStream is = new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + File.separator + fileName));
                sequencer.setSequence(is);
                currentSong = fileName;
                mudclient.getInstance().selectedSong = fileName;
                System.out.println("[Music Player] Now playing: " + currentSong);
            } catch(IOException | InvalidMidiDataException e) {
                e.printStackTrace();
            }
            sequencer.start();
        }).start();
    }

    public void stop() {
        if(debug){
            return;
        }

        sequencer.stop();
    }
    
    public boolean isRunning() {
        if(debug){
            return debug;
        }
        return sequencer.isRunning();
    }
    
    public void close() {
        if(debug){
            return;
        }
        sequencer.close();
    }

}