package de.operatorplease.sprinkler.settings;

public class Zone {
	private int zid;
	private boolean disabled;
	
	public Zone(int zid) {
		this.zid = zid;
	}
	
	public int getZid() {
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
