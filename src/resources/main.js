let name = '';
let serverIP = 'http://10.243.158.202:8080';

let zoneID = 0;
let entityID = 0;

function toggleEditable(element) {
    let elementTag = 'p';
    if (element.tagName === 'P') {
        elementTag = 'input';
    } else {
        name = element.value;
    }

    let newElement = document.createElement(elementTag);
    newElement.addEventListener('dblclick', function (ev) {
        toggleEditable(newElement)
    });
    newElement.id = 'name';

    if (element.tagName === 'P') {
        newElement.type = 'text';
        newElement.placeholder = 'No Name';
        newElement.value = name;
    } else {
        newElement.innerHTML = name;
        let data = {type: 'name', name: name};
        console.log(JSON.stringify(data));
        let _ = fetch(`${serverIP}/entities/${zoneID}/${entityID}`, { method: 'PATCH', body: JSON.stringify(data) });
    }

    element.parentNode.replaceChild(newElement, element);
}

window.addEventListener('load', function () {
    console.log('Loaded');

    // Get a free entity from the server
    fetch(`${serverIP}/entities/0`, { method: 'POST', mode: 'cors'}).then(response => {
        response.text().then(data => {
            console.log(JSON.parse(data));
        })
    });

    // Set toggleable name
    let nameElement = document.querySelector('#name');
    name = nameElement.innerHTML;
    nameElement.addEventListener('dblclick', function (ev) {
        toggleEditable(nameElement)
    });
});