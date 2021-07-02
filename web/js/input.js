function getInputElement(id) {
    const element = document.getElementById(id);
    if (element == null) {
        console.error('input element "' + id + '" not found');
        // return dummy
        return document.createElement('input');
    }
    return element;
}
export class RadioGroup {
    constructor(input) {
        this.input = input;
    }
    get value() {
        let val = null;
        this.input.forEach(rb => {
            const inp = rb;
            if (inp.checked) {
                val = inp.value;
            }
        });
        return val;
    }
    set value(value) {
        let found = false;
        this.input.forEach(rb => {
            const inp = rb;
            inp.checked = value === inp.value;
            found = found || value === inp.value;
        });
        if (!found && value)
            console.warn("value could not be assigned", value);
    }
    static find(group) {
        let elements = document.querySelectorAll('input[name="' + group + '"]');
        console.log("radio group", group, elements);
        return new RadioGroup(elements);
    }
}
export class Text {
    constructor(input) {
        this.input = input;
    }
    get value() {
        return this.input.value;
    }
    set value(val) {
        this.input.value = val;
    }
    static find(id) {
        return new Text(getInputElement(id));
    }
}
export class Selection {
    constructor(input) {
        this.input = input;
    }
    get element() {
        return this.input;
    }
    get value() {
        return this.input.selectedIndex === -1 ? null : this.input.options[this.input.selectedIndex].value;
    }
    set value(value) {
        if (!value) {
            this.input.selectedIndex = -1;
        }
        for (let opt of this.input.options) {
            if (opt.value === value) {
                this.input.selectedIndex = opt.index;
                return;
            }
        }
        console.warn("value could not be assigned", value);
    }
    setOptions(opts) {
        while (this.input.options.length > 0)
            this.input.options.remove(0);
        opts.forEach(o => {
            let el = document.createElement('option');
            el.value = o.id;
            el.textContent = o.name;
            this.input.options.add(el);
        });
    }
    static find(id) {
        let select;
        const element = document.getElementById(id);
        if (element == null) {
            console.error('input element "' + id + '" not found');
            // return dummy
            select = document.createElement('select');
        }
        select = element;
        return new Selection(select);
    }
}
export class Checkbox {
    constructor(input) {
        this.input = input;
    }
    get value() {
        return this.input.checked;
    }
    set value(checked) {
        this.input.checked = checked;
    }
    static find(id) {
        return new Checkbox(getInputElement(id));
    }
}
