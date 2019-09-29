let name = '';
let serverIP = 'http://10.243.158.202:8080';

let zoneID = 0;
let entityID = 0;

function toggleEditable(element) {
    if(element.tagName === 'INPUT' && element.value.length < 1) {
        return;
    }

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
        let _ = fetch(`${serverIP}/entities/${zoneID}/${entityID}`, { method: 'PATCH', body: JSON.stringify(data) });
    }

    element.parentNode.replaceChild(newElement, element);
}

function updateIDs() {
    let zoneElement = document.querySelector('#zone');
    let entityElement = document.querySelector('#entity');

    zoneElement.innerHTML = zoneID;
    entityElement.innerHTML = entityID;
}

function parseMessage(message) {
    let json = JSON.parse(message.data);
    // console.log(json);

    let cat = json['cat'];
    switch (cat) {
    case 'Alert': newAlertRow(json); break;
    case 'Move': zoneID = json['newZone']; entityID = json['newEntity']; updateIDs(); break;
    case 'NewZone': newZoneRow(json); break;
    }
}

function newAlertRow(json) {
    let keys = ['type', 'severity', 'zoneID'];

    let tableBody = document.querySelector('#alertBody');
    let row = document.createElement('tr');
    tableBody.appendChild(row);

    for (let i = 0; i < 3; ++i) {
        let cell = document.createElement('td');
        cell.innerHTML = json[keys[i]];
        row.appendChild(cell);
    }
}

const zoneNames = ["Office", "Bow", "Stern", "Port", "Aft"];

function newZoneRow(json) {
    let tableBody = document.querySelector('#zoneBody');
    let row = document.createElement('tr');
    tableBody.appendChild(row);

    let zoneID = json['zoneID'];
    let roomName = zoneID <= zoneNames.length ? zoneNames[zoneID - 1] : "Place";

    let cells = Array(4);
    for (let i = 0; i < 4; ++i) {
        cells[i] = document.createElement('td');
    }
    cells[0].innerHTML = zoneID;
    cells[1].innerHTML = roomName;
    cells[2].innerHTML = 0;
    cells[2].id = `occupancy${zoneID}`;
    cells[3].innerHTML = "Clear"
    for (let i = 0; i < 4; ++i) {
        row.appendChild(cells[i]);
    }
}

window.addEventListener('load', async function () {
    console.log('Loaded');

    // Get a free entity from the server
    let response = await fetch(`${serverIP}/entities/0`, {method: 'POST', mode: 'cors'});
    let data = await response.text();
    let jsonData = JSON.parse(data);
    zoneID = jsonData['zoneID'];
    entityID = jsonData['id'];
    updateIDs();

    // Create zone rows
    let json = await (await fetch(`${serverIP}/zones/total`)).json();
    let n = json['zoneCount'];
    for (let i = 1; i < n; ++i) {
        newZoneRow({zoneID: i});
    }

    // Set toggleable name
    let nameElement = document.querySelector('#name');
    name = nameElement.value;
    nameElement.addEventListener('dblclick', function (ev) {
        toggleEditable(nameElement)
    });

    // Start websocket
    let socket = new WebSocket(`ws${serverIP.substring(4)}/sockets/${zoneID}/${entityID}`);
    socket.onopen = ev => {
        socket.onmessage = message => parseMessage(message);
        socket.send(JSON.stringify({zoneID: zoneID, entityID: entityID, type: 'Electrocution', severity: 'All'}));
    };

    // Things to do regularly
    let interval = setInterval(async () => {
        // Update occupancies
        let json = await (await fetch(`${serverIP}/zones/total`)).json();
        let n = json['zoneCount'];
        for (let i = 0; i < n; ++i) {
            let zone = await (await fetch(`${serverIP}/zones/${i}`)).json();
            document.querySelector(`#occupancy${i}`).innerHTML = zone['entities'].length;
        }

        // clearInterval(interval);
    },2000);
});