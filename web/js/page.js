let navigator;
ons.ready(() => {
    navigator = document.querySelector("ons-navigator");
});
const pages = new Map();
export function awaitInit(page, cb) {
    pages.set(page, cb);
}
export function pushPage(page, options) {
    navigator.pushPage(page, {
        data: options
    });
}
export function getData() {
    return navigator.topPage.data;
}
document.addEventListener("init", function (event) {
    const target = event.target;
    console.log("init", target.id);
    const cb = pages.get(target.id);
    if (cb) {
        try {
            ons.ready(() => {
                console.log("ready", target.id);
                cb();
            });
        }
        catch (e) {
            console.error(e);
            ons.notification.alert(e.message);
        }
    }
}, false);
