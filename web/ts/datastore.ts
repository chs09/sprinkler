export interface ZoneEntry {
	id: string,
	name: string
}

let zones: ZoneEntry[] = [
	{ id: "Z1", name: "Zone 1" },
	{ id: "Z2", name: "Zone 2" },
	{ id: "Z3", name: "Zone 3" },
	{ id: "Z4", name: "Zone 4" },
	{ id: "Z5", name: "Zone 5" }
];

class ZonesStore {
	list(): ZoneEntry[] {
		return zones;
	}
}
export const Zones = new ZonesStore();

export interface ScheduleEntry {
	id: number,
	name: string
	enabled: boolean;
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
	list() : ScheduleEntry[] {
		return schedules;
	}

	delete(entry: ScheduleEntry) {
		const index = schedules.indexOf(entry);
		if (index > -1) {
			schedules.splice(index, 1);
		}
	}

	get(id: number) : ScheduleEntry | null {
		let result = schedules.filter(s => s.id === id);
		return result.length > 0 ? result[0] : null;
	}
}
export const Schedules = new SchedulesStore();