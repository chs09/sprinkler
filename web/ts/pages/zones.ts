import * as Store from "../datastore.js";
import * as Page from "../page.js"

//@ts-ignore, wrap global to prevent ts error
declare const ons: ons;

Page.awaitInit("page-zones", async () => {
	console.log("ready!");

	let zones = await Store.Zones.list();
	initPage(zones);
});

function initPage(zones: Store.ZoneEntry[]) {
	let zonesList = document.getElementById('zones-list') as ons.OnsLazyRepeatElement;
	zonesList.delegate = {
		createItemContent: i => {
			const id = zones[i].id;
			let disabled = '';
			let checked = zones[i].running === true ? 'checked' : '';
			let element = ons.createElement(
				`<ons-list-item ${disabled}>
					<label for="t${id}" class="left">${zones[i].name}</label>
					<div class="right" ${disabled}>
						<ons-switch input-id="switch_${id}" modifier="material" ${checked} ${disabled}></ons-switch>
					</div>
				</ons-list-item>`) as HTMLElement;

				let btn = element.querySelector(`[input-id=switch_${id}]`) as HTMLInputElement;
				btn.onclick = () => {
					console.log(`${id} ` + btn.checked);
					if(btn.checked) {
						Store.Zones.start(id).then(load);
					} else {
						Store.Zones.stop(id).then(load);
					}
				}
				return element;
		},
		countItems: () => { return zones.length; },
	};
	zonesList.refresh();
	load();

	let buttonReset = document.getElementById('btn_reset');
	(buttonReset as HTMLButtonElement).onclick = () => {
		// Stop All Zones, then (re)load status
		load();
	};
};

function load() {
	// update running states
	Store.Zones.list().then(zones => {
		for(let zone of zones) {
			const id = zone.id;
	
			let btn = document.querySelector(`[input-id=switch_${id}]`) as HTMLInputElement;
			btn.checked = zone.running === true;
		}
	});
}
