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
import { Text, Checkbox, Selection } from "../input.js";
let before;
let entry;
let zones;
let name;
let enabled;
let adjust;
let days;
let main;
let times = new Map();
Page.awaitInit("page-sched-edit", () => __awaiter(void 0, void 0, void 0, function* () {
    console.log("ready!");
    let element;
    let data = Page.getData();
    if (data.id === -1) {
        element = {
            id: -1
        };
    }
    else {
        element = yield Store.Schedules.get(data.id);
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
    zones = yield Store.Zones.list();
    initZones();
    let zonesList = document.getElementById('zones-list');
    zonesList.delegate = {
        createItemContent: i => {
            const id = zones[i].id;
            let disabled = main.value === id ? 'disabled' : '';
            let value = main.value === id ? null : times.get(id);
            if (!value) {
                value = 0;
            }
            let element = ons.createElement(`<ons-list-item class="center" ${disabled}>
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
				</ons-list-item>`);
            let input = element.querySelector(`[input-id=t${id}]`);
            let interval;
            input.onmousedown = () => {
                interval = setInterval(() => {
                    element.querySelector(`#l${id}`).textContent = '' + input.value;
                }, 100);
            };
            input.onmouseup = () => {
                clearInterval(interval);
            };
            input.onchange = () => {
                if (input.value && input.value !== '0') {
                    times.set(id, Number.parseInt(input.value));
                }
                else {
                    times.delete(id);
                }
                element.querySelector(`#l${id}`).textContent = '' + input.value;
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
    buttonReset.onclick = () => {
        reset();
    };
}));
function initZones() {
    let opts = zones.map(z => {
        return {
            id: z.id,
            name: z.name
        };
    });
    opts.unshift({
        id: 'NONE',
        name: 'None'
    });
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
