var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import * as Store from "../datastore.js";
import * as Page from "../page.js";
Page.awaitInit("page-zones", () => __awaiter(void 0, void 0, void 0, function* () {
    console.log("ready!");
    let zones = yield Store.Zones.list();
    initPage(zones);
}));
function initPage(zones) {
    let zonesList = document.getElementById('zones-list');
    zonesList.delegate = {
        createItemContent: i => {
            const id = zones[i].id;
            let disabled = '';
            let checked = zones[i].running === true ? 'checked' : '';
            let element = ons.createElement(`<ons-list-item ${disabled}>
					<label for="t${id}" class="left">${zones[i].name}</label>
					<div class="right" ${disabled}>
						<ons-switch input-id="switch_${id}" modifier="material" ${checked} ${disabled}></ons-switch>
					</div>
				</ons-list-item>`);
            let btn = element.querySelector(`[input-id=switch_${id}]`);
            btn.onclick = () => {
                console.log(`${id} ` + btn.checked);
                if (btn.checked) {
                    Store.Zones.start(id).then(load);
                }
                else {
                    Store.Zones.stop(id).then(load);
                }
            };
            return element;
        },
        countItems: () => { return zones.length; },
    };
    zonesList.refresh();
    load();
    let buttonReset = document.getElementById('btn_reset');
    buttonReset.onclick = () => {
        // Stop All Zones, then (re)load status
        load();
    };
}
;
function load() {
    // update running states
    Store.Zones.list().then(zones => {
        for (let zone of zones) {
            const id = zone.id;
            let btn = document.querySelector(`[input-id=switch_${id}]`);
            btn.checked = zone.running === true;
        }
    });
}
