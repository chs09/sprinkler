package de.operatorplease.sprinkler;

// dummy weather provider, always returning 100%
public class Weather {
	public float getAdjustment() {
		return 1.0f;
	}
	
	public void check() {
		
	}
}