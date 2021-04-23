import * as Page from "./page.js"
import * as Store from "./datastore.js";

//@ts-ignore, wrap global to prevent ts error
declare const ons: ons;

Page.awaitInit("page-sched-list", () => {
	update();
});

function update() {
	const list = Store.Schedules.list();
	console.log(list);

	const htmlList = document.getElementById("schedule_list");
	if (!htmlList) {
		throw "schedule_list not found";
	}

	const old = htmlList.querySelectorAll('[dynamic]');
	console.log(old);
	old.forEach((e) => e.remove());

	for (let sched of list) {
		const template = /*html*/ `
		<ons-list-item tappable dynamic>
			<div class="left">
				<ons-icon icon="fa-edit"></ons-icon>
			</div>
			<label class="center">${sched.name}</label>

			<ons-toolbar-button class="right" title="Schedule Actions">
				<ons-icon icon="fa-bars"></ons-icon>
			</ons-toolbar-button>
		</ons-list-item>`;

		const element = ons.createElement(template) as HTMLElement;
		element.onclick = () => {
			Page.pushPage('sched_edit.html', { id: sched.id });
		}
		const button = element.querySelector('ons-toolbar-button') as HTMLElement;
		button.onclick = (event) => {
			// prevent tap event
			event.stopPropagation();
			openScheduleActions(sched);
		};
		htmlList.append(element);
	}
}

function openScheduleActions(sched: Store.ScheduleEntry) {
	const buttons = [
		{
			label: 'Edit',
			icon: 'fa-edit',
			onclick: () => {
				Page.pushPage('sched_edit.html', { id: sched.id });
			}
		},
		{
			label: 'Run now',
			icon: 'fa-play',
			onclick: () => { }
		},
		{
			label: 'Delete',
			modifier: 'destructive',
			icon: 'fa-trash',
			onclick: () => {
				Store.Schedules.delete( sched );
				update();
			 }
		},
		{
			label: 'Cancel',
			icon: 'fa-close',
			onclick: () => { }
		}
	];

	ons.openActionSheet({
		title: sched.name + ' Actions',
		cancelable: true,
		buttons
	}).then(function (index: number) {
		console.log('button: ', buttons[index].label);
		buttons[index].onclick();
	});
}