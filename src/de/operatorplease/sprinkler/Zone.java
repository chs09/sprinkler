package de.operatorplease.sprinkler;

public abstract class Zone {
	private int zid;
	private boolean disabled;
	
	public Zone(int zid) {
		this.zid = zid;
	}
	
	public int getZid() {
		return zid;
	}
	
	public boolean isActive() {
		return false;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public abstract void stop();
	public abstract void start();
	
	@Override
	public String toString() {
		return "Zone[" + zid + "]";
	}
}
