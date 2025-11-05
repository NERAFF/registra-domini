const API_URI = "http://localhost:8080";

// Utility: formatta una data ISO in stringa leggibile (opzionale)
function formatDate(dateStr) {
    if (!dateStr) return "N/A";
    const d = new Date(dateStr);
    return d.toLocaleDateString('it-IT');
}

// === 1. Blocca (avvia acquisizione) un dominio ===
async function lockDominio(dominio, userId, tipologia) {
    // tipologia: "acquisto" → registrazione, "rinnovo" → non usato qui (rinnovo è PUT)
    try {
        const response = await fetch(`${API_URI}/domains/new`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                domainName: dominio,
                userId: userId,
                requestAction: "ACQUIRING"  // Avvia il lock
            })
        });

        const errDiv = document.getElementById('err');
        errDiv.innerText = '';

        if (response.ok) {
            console.log('Dominio bloccato con successo');
        } else if (response.status === 409) {
            errDiv.innerText = 'Il dominio non è disponibile per l’acquisto.';
        } else if (response.status === 400) {
            errDiv.innerText = 'Parametri non validi.';
        } else {
            errDiv.innerText = 'Errore durante il blocco del dominio.';
        }
    } catch (error) {
        console.error('Errore di rete:', error);
        document.getElementById('err').innerText = 'Errore di connessione al server.';
    }
}

// === 2. Cerca un dominio ===
async function cercaDominio(userId) {
    const dominio = document.getElementById('dom').value.trim();
    if (!dominio) return;
    await ricercaDominio(dominio, userId);
}

async function ricercaDominio(dominio, userId) {
    const errDiv = document.getElementById('err');
    const risultatoDiv = document.getElementById('risultato');
    const acqDiv = document.getElementById('acq');

    // Pulisci
    errDiv.innerText = '';
    risultatoDiv.innerText = '';
    acqDiv.innerHTML = '';

    try {
        const response = await fetch(`${API_URI}/domains/${encodeURIComponent(dominio)}`);

        if (response.status === 404) {
            // Dominio NON esiste → disponibile
            risultatoDiv.innerText = 'Il dominio è libero';
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.innerText = 'Acquista';
            btn.addEventListener('click', () => lockDominio(dominio, userId, 'acquisto'));
            acqDiv.appendChild(btn);
        } else if (response.ok) {
            const data = await response.json(); // { name, status, lastContract: { owner: { name, surname, email }, expirationDate }, ... }

            if (data.status === 'REGISTERED' || data.status === 'ACQUIRING') {
                risultatoDiv.innerText = 'Il dominio è occupato da:';
                const owner = data.lastContract?.owner || {};
                const expDate = data.lastContract?.expirationDate || 'N/A';
                const p = document.createElement('p');
                p.innerText = `${owner.name || 'N/A'} ${owner.surname || 'N/A'} (${owner.email || 'N/A'}) e scade il ${formatDate(expDate)}`;
                acqDiv.appendChild(p);
            } else if (data.status === 'EXPIRED') {
                risultatoDiv.innerText = 'Il dominio è scaduto e può essere riacquistato.';
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.innerText = 'Acquista';
                btn.addEventListener('click', () => lockDominio(dominio, userId, 'acquisto'));
                acqDiv.appendChild(btn);
            }
        } else if (response.status === 400) {
            errDiv.innerText = 'Nome dominio non valido.';
        } else {
            errDiv.innerText = 'Errore nella ricerca del dominio.';
        }
    } catch (error) {
        console.error('Errore di rete:', error);
        errDiv.innerText = 'Impossibile contattare il server.';
    }
}

// === 3. Recupera i domini dell'utente tramite operazioni di tipo REGISTRATION/RENEWAL ===
async function getDomini(userId) {
    // L'API corretta per ottenere i domini di un utente è /users/{userId}/domains
    try {
        const response = await fetch(`${API_URI}/users/${userId}/domains`);
        if (response.ok) {
            const domains = await response.json(); // L'endpoint restituisce direttamente l'array di domini
            return domains;
        } else {
            console.error('Errore nel recupero domini:', response.status);
            return [];
        }
    } catch (error) {
        console.error('Errore di rete nel recupero dei domini:', error);
        return [];
    }
}
// === 4. Aggiungi dominio alla tabella ===
function addDomini(dominio, userId) {
    const tab = document.getElementById("domini-utente");
    const riga = tab.insertRow();

    riga.insertCell().innerText = dominio.name || 'N/A';
    riga.insertCell().innerText = formatDate(dominio.lastContract?.acquisitionDate);
    riga.insertCell().innerText = formatDate(dominio.lastContract?.expirationDate);

    const oggi = new Date();
    const scadenza = new Date(dominio.lastContract?.expirationDate);
    const cell = riga.insertCell();

    if (oggi <= scadenza) {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.innerText = "Rinnova";
        btn.addEventListener("click", () => {
            // NOTA: il rinnovo NON usa lock, ma una PUT diretta.
            // Potresti voler reindirizzare a una pagina dedicata.
            alert(`Rinnovo per ${dominio.name} non ancora implementato nel frontend.`);
            // Oppure: window.location.href = `rinnovo.html?dominio=${dominio.name}&id=${userId}`;
        });
        cell.appendChild(btn);
    } else {
        cell.innerText = "Scaduto";
    }
}

// === 5. Recupera ordini (operazioni) ===
async function getOrdini(userId) {
    try {
        const response = await fetch(`${API_URI}/operations?userId=${userId}`);
        if (response.ok) {
            return await response.json();
        }
        return [];
    } catch (error) {
        console.error('Errore nel recupero degli ordini:', error);
        return [];
    }
}

function addOrdini(ordine) {
    const tab = document.getElementById("ordini-utente");
    if (!tab) return;
    const riga = tab.insertRow();
    // I campi provengono dal modello OperationInfo
    riga.insertCell().innerText = ordine.domainName || 'N/A';
    riga.insertCell().innerText = formatDate(ordine.timestamp); // L'API non sembra avere 'timestamp' ma lo assumiamo
    riga.insertCell().innerText = ordine.type || 'N/A';
    riga.insertCell().innerText = ordine.cost ? `${ordine.cost.toFixed(2)} €` : 'N/A';
}

// === 6. Inizializzazione ===
async function init() {
    // Usiamo un userId statico come richiesto per il momento
    const userId = 1;

    // Carica e mostra i domini dell'utente
    const domini = await getDomini(userId);
    domini.forEach(d => addDomini(d, userId));

    // Carica e mostra gli ordini dell'utente
    const ordini = await getOrdini(userId);
    ordini.forEach(o => addOrdini(o));

    // Aggiungi event listener per la ricerca
    const form = document.getElementById("cerca-domini");
    if (form) {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            cercaDominio(userId);
        });
    }

    // Aggiungi event listener per il logout
    const esciLink = document.getElementById("esci");
    if (esciLink) {
        esciLink.addEventListener("click", (e) => {
            e.preventDefault();
            localStorage.removeItem('sessionId');
            window.location.href = "./auth/login.html"; // Reindirizza al login
        });
    }
}

window.onload = init;