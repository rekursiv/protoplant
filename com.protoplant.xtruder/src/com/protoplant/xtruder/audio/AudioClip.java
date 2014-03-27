package com.protoplant.xtruder.audio;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioClip implements LineListener {

	private static Logger log = Logger.getLogger(AudioClip.class.getName());
	private Clip clip = null;
	private String fileName = null;
	
	public AudioClip(String fileName) {
		this.fileName = fileName;
		try {
			clip = AudioSystem.getClip();
			clip.addLineListener(this);
		} catch (LineUnavailableException e) {
			log.log(Level.WARNING, "", e);
		}
	}
	
	@Override
	public void update(LineEvent event) {
//		log.info(event.toString());
		if (event.getType()==Type.STOP) close();
	}
	
	public void load() throws Exception {
        URL url = AudioClip.class.getResource(fileName);
        if (url==null) {
        	log.warning("Not found: "+fileName);
        	return;
        }

//        AudioInputStream audioIn = AudioSystem.getAudioInputStream(Paths.get("c:/temp/hello.wav").toUri().toURL());
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
        
        clip.open(audioIn);
        audioIn.close();
	}

	public void play() {
		if (clip.isActive()) return;
		try {
			if (!clip.isOpen()) load(); 
		} catch (Exception e) {
			log.log(Level.WARNING, "Error loading clip: ", e);
		}
		
		clip.setFramePosition(0);
		clip.start();
		
	}

	public void close() {
		if (clip!=null && clip.isOpen()) clip.close();
	}
	
}
