import { resolveLoadingPlaceholder } from "../lib/onsen/js/onsenui";

export interface ZoneEntry {
	id: string,
	name: string,
	running: boolean
}

let zones: ZoneEntry[] = [
	{ id: "Z1", name: "Zone 1", running: false },
	{ id: "Z2", name: "Zone 2", running: false },
	{ id: "Z3", name: "Zone 3", running: false },
	{ id: "Z4", name: "Zone 4", running: false },
	{ id: "Z5", name: "Zone 5", running: false }
];

class ZonesStore {
	list(): Promise<ZoneEntry[]> {
		return new Promise( (resolve, reject) => {
			resolve(zones);
		});
	}

	start(id: string) {
		return fetch(`/api/valve/${id}/open`, { method: 'POST' });
	}

	stop(id: string) {
		return fetch(`/api/valve/${id}/close`, { method: 'POST' });
	}
}
export const Zones = new ZonesStore();

export interface ScheduleEntry {
	id: number,
	name: string
	enabled: boolean;
	durations?: Map<string, number>;
}

let schedules: ScheduleEntry[] = [
		{
			id: 1,
			name: 'Frontyard',
			enabled: true
		},
		{
			id: 2,
			name: 'Backyard',
			enabled: true
		}
];

class SchedulesStore {
	list() : Promise<ScheduleEntry[]> {
		return Promise.resolve(schedules);
	}

	delete(entry: ScheduleEntry) {
		const index = schedules.indexOf(entry);
		if (index > -1) {
			schedules.splice(index, 1);
		}
	}

	get(id: number) : Promise<ScheduleEntry> {
		let result = schedules.filter(s => s.id === id);
		return result.length > 0 ? Promise.resolve(result[0]) : Promise.reject();
	}
}
export const Schedules = new SchedulesStore();