package de.operatorplease.sprinkler.tinker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tinkerforge.BrickletIndustrialQuadRelayV2;
import com.tinkerforge.Device.Identity;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Station;
import de.operatorplease.sprinkler.settings.Zone;

public class QuadStationRelay {
	private final Logger logger = Logger.getLogger(QuadStationRelay.class.getSimpleName());
	
	private BrickletIndustrialQuadRelayV2 relay;

	private class RelayStation extends Station {
		private final int index;
		
		public RelayStation(Identity identity, int index) throws TinkerforgeException {
			super(new Zone(Uid.of(identity, index)));
			this.index = index;
		}

		@Override
		public boolean isActive() {
			try {
				return relay.getValue()[index];
			} catch (TinkerforgeException e) {
				logger.log(Level.SEVERE, "Unable to get station status ", e);
				return false;
			}
		}

		@Override
		public void stop() {
			try {
				relay.setSelectedValue(index, false);
			} catch (TinkerforgeException e) {
				logger.log(Level.SEVERE, "Unable to activate station ", e);
			}
		}

		@Override
		public void start() {
			try {
				relay.setSelectedValue(index, true);
			} catch (TinkerforgeException e) {
				logger.log(Level.SEVERE, "Unable to deactivate station ", e);
			}
		}

		@Override
		protected void toggle() {
			try {
				synchronized(relay) {
					boolean[] values = relay.getValue();
					values[index] = !values[index];
					relay.setValue(values);
				}
			} catch (TinkerforgeException e) {
				logger.log(Level.SEVERE, "Unable to toggle station ", e);
			}
		}		
	}
	
	private final List<Station> stations = new ArrayList<Station>();
	
	public QuadStationRelay(BrickletIndustrialQuadRelayV2 relay) throws TinkerforgeException {
		this.relay = relay;
		
		Identity identity = relay.getIdentity();
		for(int i=0; i<4; i++) {
			stations.add(new RelayStation(identity, i));
		}
	}
	
	public List<Station> getStations() {
		return Collections.unmodifiableList(stations);
	}
}
