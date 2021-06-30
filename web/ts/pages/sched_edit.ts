import * as Store from "../datastore.js";
import * as Page from "../page.js"

import { Text, Checkbox, Selection, RadioGroup } from "../input.js";

//@ts-ignore, wrap global to prevent ts error
declare const ons: ons;

let before: Store.ScheduleEntry;
let entry: Store.ScheduleEntry;
let zones: Store.ZoneEntry[];

let name: Text;
let enabled: Checkbox;
let adjust: Checkbox;
let days: Checkbox[];
let main: Selection;

let times: Map<string, number> = new Map();

Page.awaitInit("page-sched-edit", async () => {
	console.log("ready!");

	let element: Store.ScheduleEntry;
	let data = Page.getData();
	if (data.id === -1) {
		element = {
			id: -1
		} as Store.ScheduleEntry;
	} else {
		element = await Store.Schedules.get(data.id);
	}

	if (!element) {
		throw new Error('Could not resolve schedule with id ' + data.id);
	}

	before = Object.assign({}, element);
	entry = element;
	name = Text.find("name");
	enabled = Checkbox.find("enabled");
	adjust = Checkbox.find("adjust");
	days = [];
	for (let i = 1; i <= 7; i++) {
		days.push(Checkbox.find("d" + i));
	}
	main = Selection.find('choose-main');

	zones = await Store.Zones.list();
	initZones();

	let zonesList = document.getElementById('zones-list') as ons.OnsLazyRepeatElement;
	zonesList.delegate = {
		createItemContent: i => {
			const id = zones[i].id;
			let disabled = main.value === id ? 'disabled' : '';
			let value = main.value === id ? null : times.get(id);
			if(!value) {
				value = 0;
			}
			let element = ons.createElement(
				`<ons-list-item class="center" ${disabled}>
				<label for="t${id}" class="left">${zones[i].name}</label>
				<ons-row class="center">
					<ons-col width="40px" style="text-align: center; line-height: 31px;">
						<ons-icon icon="fa-tint-slash"></ons-icon>
					</ons-col>
					<ons-col>
						<ons-range value="${value}" max="240" min="0" modifier="material" style="width: 100%;" input-id="t${id}"></ons-range>
					</ons-col>
					<ons-col width="40px" style="text-align: center; line-height: 31px;">
						<ons-icon icon="fa-tint"></ons-icon>
					</ons-col>
				</ons-row>
				<label id="l${id}" class="right" style="width:50px">${value}</label>
				</ons-list-item>`
			) as HTMLElement;
			let input = element.querySelector(`[input-id=t${id}]`) as HTMLInputElement;
			let interval: number;
			input.onmousedown = () => {
				interval = setInterval(() => {
					(element.querySelector(`#l${id}`) as HTMLElement).textContent = '' + input.value;
				}, 100);
			}
			input.onmouseup = () => {
				clearInterval(interval);
			}
			input.onchange = () => {
				if(input.value && input.value !== '0') {
					times.set(id, Number.parseInt(input.value));
				} else {
					times.delete(id);
				}
				(element.querySelector(`#l${id}`) as HTMLElement).textContent = '' + input.value;
			};
			return element;
		},
		countItems: () => { return zones.length; },
	};
	zonesList.refresh();

	main.element.onchange = () => {
		zonesList.refresh();
	};

	load();

	let buttonReset = document.getElementById('btn_reset');
	(buttonReset as HTMLButtonElement).onclick = () => {
		reset();
	};
});

function initZones() {
	let opts = zones.map(z => {
		return {
			id: z.id,
			name: z.name
		}
	});
	opts.unshift({
		id: 'NONE',
		name: 'None'
	})
	main.setOptions(opts);
}

function reset() {
	Object.assign(entry, before);
	load();
}

function load() {
	enabled.value = entry.enabled;
	name.value = entry.name;
	times = (entry.durations) ? entry.durations : new Map();
}

function save() {
	entry.enabled = enabled.value;
	entry.name = name.value;
	entry.durations = times;
}