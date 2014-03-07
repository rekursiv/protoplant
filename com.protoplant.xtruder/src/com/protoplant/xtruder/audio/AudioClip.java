package com.protoplant.xtruder.audio;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioClip {

	private static Logger log = Logger.getLogger(AudioClip.class.getName());
	private Clip clip = null;
	private String fileName = null;
	
	public AudioClip(String fileName) {
		this.fileName = fileName;
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load() throws Exception {
        URL url = AudioClip.class.getResource(fileName);
        if (url==null) {
        	log.warning("Not found: "+fileName);
        	return;
        }

//        AudioInputStream audioIn = AudioSystem.getAudioInputStream(Paths.get("c:/temp/hello.wav").toUri().toURL());
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
        
        clip = AudioSystem.getClip();
        clip.open(audioIn);
        audioIn.close();
	}
	
	public void play() {
		if (clip!=null) {
			clip.setFramePosition(0);
			clip.start();
		}
	}

	public void close() {
		if (clip!=null) clip.close();
	}
	
}
