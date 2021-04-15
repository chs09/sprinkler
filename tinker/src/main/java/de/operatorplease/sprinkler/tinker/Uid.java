package de.operatorplease.sprinkler.tinker;

import com.tinkerforge.Device.Identity;

public class Uid {
	public static String of(Identity identity) {
		return identity.uid;
	}
	
	public static String of(Identity identity, int index) {
		// Sensors and stations must always be able to be assigned in the same order
		int pos = identity.position - 'A';
		int id = (pos * 1000) + index;
		return id + identity.uid;
	}
}
