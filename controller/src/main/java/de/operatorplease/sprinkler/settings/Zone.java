package de.operatorplease.sprinkler.settings;

public class Zone {
	private String zid;
	private boolean disabled;
	
	public Zone(String uid) {
		this.zid = uid;
	}
	
	public String getZid() {
		return zid;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public String toString() {
		return "Zone[zid=" + zid + (disabled ? ", disabled": "") + "]";
	}
}
