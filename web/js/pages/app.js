import './sched_list.js';
import './sched_edit.js';
import './zones.js';
//@ts-ignore, wrap global to prevent ts error
const onsen = ons;
let navigatorElement;
function getTemplate(id) {
    return document.querySelector('#' + id);
}
onsen.ready(() => {
    var _a, _b;
    console.log("ready");
    navigatorElement = document.querySelector('ons-navigator');
    (_a = document.querySelector('#action_schedules')) === null || _a === void 0 ? void 0 : _a.addEventListener('click', () => {
        navigatorElement.pushPage('sched_list.html');
    });
    (_b = document.querySelector('#action_zones')) === null || _b === void 0 ? void 0 : _b.addEventListener('click', () => {
        navigatorElement.pushPage('zones.html');
    });
});
