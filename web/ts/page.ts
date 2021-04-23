//@ts-ignore, wrap global to prevent ts error
export declare const ons: ons;
let navigator: ons.OnsNavigatorElement;
ons.ready(() => {
	navigator = document.querySelector(
		"ons-navigator"
	) as ons.OnsNavigatorElement;
});

type callback = () => void;
const pages: Map<string, callback> = new Map();

export function awaitInit(page: string, cb: callback) {
	pages.set(page, cb);
}

export function pushPage(page: string, options?: any) {
	navigator.pushPage(page, {
		data: options
	});
}

export function getData() {
	return (navigator.topPage as any).data
}

document.addEventListener(
	"init",
	function (event) {
		const target = event.target as HTMLElement;

		console.log("init", target.id);
		const cb = pages.get(target.id);
		if (cb) {
			try {
				ons.ready(() => {
					console.log("ready", target.id);
					cb();
				});
			} catch (e) {
				console.error(e);
				ons.notification.alert((e as Error).message);
			}
		}
	},
	false
);