import './sched_list.js';
import './sched_edit.js';
import './zones.js';

//@ts-ignore, wrap global to prevent ts error
const onsen = ons;

let navigatorElement: ons.OnsNavigatorElement;

function getTemplate(id: string): ons.OnsTemplateElement {
	return document.querySelector('#' + id) as ons.OnsTemplateElement;
}

onsen.ready(() => {
	console.log("ready");

	navigatorElement = document.querySelector('ons-navigator') as ons.OnsNavigatorElement;

	document.querySelector('#action_schedules')?.addEventListener('click', () => {
		navigatorElement.pushPage('sched_list.html');
	});

	document.querySelector('#action_zones')?.addEventListener('click', () => {
		navigatorElement.pushPage('zones.html');
	});
});