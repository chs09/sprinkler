function getInputElement(id: string): HTMLInputElement {
	const element = document.getElementById(id);
	if(element == null) {
		console.error('input element "' + id + '" not found');
		// return dummy
		return document.createElement('input');
	}
	return element as HTMLInputElement;
}

export class RadioGroup {
    private input: NodeListOf<Element>;

    constructor(input: NodeListOf<Element>) {
		this.input = input;
	}

	get value() {
		let val = null;
		this.input.forEach(rb => {
			const inp = rb as HTMLInputElement;
			if(inp.checked) {
				val = inp.value;
			}
		})
		return val;
	}

	set value(value: string | null) {
		let found = false;
		this.input.forEach(rb => {
			const inp = rb as HTMLInputElement;
			inp.checked = value === inp.value;
			found = found || value === inp.value;
		});

		if(!found && value)
			console.warn("value could not be assigned", value);
	}

    static find(group: string) {
        let elements = document.querySelectorAll('input[name="' + group + '"]');
        console.log("radio group", group, elements);

		return new RadioGroup(elements);
	}
}

export class Text {
    private input: HTMLInputElement;

    constructor(input: HTMLInputElement) {
		this.input = input;
	}

	get value() {
		return this.input.value;
	}

	set value(val: string) {
		this.input.value = val;
	}

	static find(id: string) {
		return new Text(getInputElement(id));
	}
}

interface Option {
	id: string,
	name: string
}

export class Selection {
	private input: HTMLSelectElement;

	constructor(input: HTMLSelectElement) {
		this.input = input;
	}

	get element() : HTMLElement {
		return this.input;
	}

	get value() {
		return this.input.selectedIndex === -1 ? null : this.input.options[this.input.selectedIndex].value;
	}

	set value(value: string | null) {
		if(!value) {
			this.input.selectedIndex = -1;
		}

		for(let opt of this.input.options) {
			if(opt.value === value) {
				this.input.selectedIndex = opt.index;
				return;
			}
		}
		console.warn("value could not be assigned", value);
	}

	setOptions(opts: Option[]) {
		while(this.input.options.length > 0)
			this.input.options.remove(0);

		opts.forEach(o => {
			let el = document.createElement('option');
			el.value = o.id;
			el.textContent = o.name;

			this.input.options.add(el);
		});
	}

	static find(id: string) {
		let select: HTMLSelectElement;
		const element = document.getElementById(id);
		if(element == null) {
			console.error('input element "' + id + '" not found');
			// return dummy
			select = document.createElement('select') as HTMLSelectElement;
		}
		select = element as HTMLSelectElement;
		return new Selection(select);
	}
}

export class Checkbox {
	private input: HTMLInputElement;

	constructor(input: HTMLInputElement) {
		this.input = input;
	}

	get value() {
		return this.input.checked;
	}

	set value(checked: boolean) {
		this.input.checked = checked;
	}

	static find(id: string) {
		return new Checkbox(getInputElement(id));
	}
}