package com.protoplant.xtruder.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AudioManager {
	
	Map<String, AudioClip> clipMap = new HashMap<String, AudioClip>();
	private Logger log;
	
	@Inject
	public AudioManager(Logger log) {
		this.log = log;
		init();
	}	
	
	public void init() {
		addClip("200g");
		addClip("240g");
		addClip("900g");
		addClip("990g");
		addClip("5");
		addClip("4");
		addClip("3");
		addClip("2");
		addClip("1");
		addClip("mark");
	}
	
	public void addClip(String name) {
		clipMap.put(name, new AudioClip(name+".wav"));
	}
	
	public void destroy() {
		for (AudioClip clip : clipMap.values()) {
			clip.close();
		}
	}
	
	public void playClip(String clipName) {
		AudioClip clip = clipMap.get(clipName);
		if (clip!=null) clip.play();
		else log.warning("Clip not found: "+clipName);
	}

}
