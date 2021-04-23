import * as Store from "./datastore.js";
import * as Page from "./page.js"

import {Text, Checkbox, Selection, RadioGroup} from "./input.js";

//@ts-ignore, wrap global to prevent ts error
declare const ons: ons;

let entry: Store.ScheduleEntry;
let zones: Store.ZoneEntry[];

let name: Text;
let enabled: Checkbox;
let adjust: Checkbox;
let days: Checkbox[];
let main: Selection;
let restriction: RadioGroup;

Page.awaitInit("page-sched-edit", () => {
	console.log("ready!");

	let element: Store.ScheduleEntry | null;
	let data = Page.getData();
	if(data.id === -1) {
		element = {
			id: -1
		} as Store.ScheduleEntry;
	} else {
		element = Store.Schedules.get(data.id);
	}

	if(!element) {
		throw new Error('Could not resolve schedule with id ' + data.id);
	}

	entry = element;
	name = Text.find("name");
	enabled = Checkbox.find("enabled");
	adjust = Checkbox.find("adjust");
	days = [];
	for(let i=1; i<=7; i++) {
		days.push(Checkbox.find("d"+i));
	}
	main = Selection.find('choose-main');
	restriction = RadioGroup.find('restriction');

	zones = Store.Zones.list();
	initZones();
	load();
});

function initZones() {
	let opts = zones.map(z => {
		return {
			id: z.id,
			name: z.name
		}
	});
	main.setOptions(opts);
}

function load() {
	name.value = entry.name;
}

function save() {
	entry.name = name.value;
}