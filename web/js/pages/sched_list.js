var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
import * as Page from "../page.js";
import * as Store from "../datastore.js";
Page.awaitInit("page-sched-list", () => {
    update();
});
function update() {
    return __awaiter(this, void 0, void 0, function* () {
        const list = yield Store.Schedules.list();
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
            const element = ons.createElement(template);
            element.onclick = () => {
                Page.pushPage('sched_edit.html', { id: sched.id });
            };
            const button = element.querySelector('ons-toolbar-button');
            button.onclick = (event) => {
                // prevent tap event
                event.stopPropagation();
                openScheduleActions(sched);
            };
            htmlList.append(element);
        }
    });
}
function openScheduleActions(sched) {
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
                Store.Schedules.delete(sched);
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
    }).then(function (index) {
        console.log('button: ', buttons[index].label);
        buttons[index].onclick();
    });
}
