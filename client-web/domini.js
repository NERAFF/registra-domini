const API_URI = "http://localhost:8080";

// Utility: formatta una data ISO in stringa leggibile (opzionale)
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';

    // Pulisci il formato non standard
    const cleaned = dateStr.replace(/\[.*\]$/, '');

    const date = new Date(cleaned + 'T00:00:00');
    if (date.toString() === 'Invalid Date') {
        console.warn('Data non valida:', dateStr);
        return 'Data errata';
    }

    // Formatta in italiano: gg/mm/aaaa, hh:mm
    return new Intl.DateTimeFormat('it-IT', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
    }).format(date);
}

// === 1. Blocca (avvia acquisizione) un dominio ===
async function lockDominio(dominio, userId) {
    // tipologia: "acquisto" → registrazione, "rinnovo" → non usato qui (rinnovo è PUT)
    try {
        const response = await fetch(`${API_URI}/domains/new`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                domainName: dominio,
                userId: userId,
                requestAction: "ACQUIRING"
            })
        });

        const errDiv = document.getElementById('err');
        errDiv.innerText = '';

        if (response.ok) {
            console.log('Dominio bloccato con successo');
            return true; // Successo
        } else if (response.status === 409) {
            errDiv.innerText = 'Il dominio non è disponibile per l’acquisto.';
        } else if (response.status === 400) {
            errDiv.innerText = 'Parametri non validi.';
        } else if (response.status === 403) {
            errDiv.innerText = 'Azione non permessa. Il dominio potrebbe essere già registrato da un altro utente.';
        } else {
            errDiv.innerText = 'Errore durante il blocco del dominio.';
        }
        return false; // Fallimento
    } catch (error) {
        console.error('Errore di rete:', error);
        document.getElementById('err').innerText = 'Errore di connessione al server.';
        return false; // Fallimento
    }
}

// === 2. Cerca un dominio ===
async function cercaDominio(event, userId) {
    event.preventDefault(); // Impedisce il refresh della pagina

    const dominio = document.getElementById('dom').value.trim();
    if (!dominio) return; // Se il campo è vuoto, non fare nulla
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

        if (response.ok) {
            const data = await response.json(); // { status, ownerId, ownerName, ownerSurname, ownerEmail, expirationDate }
            
            switch (data.status) {
                case 'AVAILABLE':
                case 'EXPIRED':
                    risultatoDiv.innerText = data.status === 'AVAILABLE' 
                        ? 'Il dominio è libero.' 
                        : 'Il dominio è scaduto e può essere riacquistato.';
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.innerText = 'Acquista';
                    btn.addEventListener('click', async () => {
                        const locked = await lockDominio(dominio, userId);
                        if (locked) {
                            window.location.href = `acquisto.html?dominio=${encodeURIComponent(dominio)}&id=${userId}&tipo=acquisto`;
                        }
                    });
                    acqDiv.appendChild(btn);
                    break;

                case 'ACQUIRING':
                    if (data.owner?.id && data.owner.id.toString() === userId) {
                        risultatoDiv.innerText = 'Hai un acquisto in corso per questo dominio.';
                        const p = document.createElement('p');
                        p.innerText = `Puoi completare l'acquisto dalla pagina di acquisto.`;
                        acqDiv.appendChild(p);
                    } else {
                        risultatoDiv.innerText = 'Dominio in fase di acquisizione da:';
                        if (data.owner) {
                            const p = document.createElement('p');
                            p.innerText = `${data.owner.name} ${data.owner.surname} ${data.owner.email}`;
                            acqDiv.appendChild(p);
                        }
                    }
                    break;
                case 'REGISTERED':
                    risultatoDiv.innerText = 'Il dominio è stato già registrato:';
                    if (data.owner) {
                        const p = document.createElement('p');
                        p.innerText = `${data.owner.name} ${data.owner.surname} ${data.owner.email} ${data.expirationDate}`;
                        acqDiv.appendChild(p);
                    }
                    break;

                default:
                    errDiv.innerText = `Stato del dominio non riconosciuto: ${data.status}`;
                    break;
            }
        } else if (response.status === 400) {
            errDiv.innerText = 'Nome dominio non valido.';
        } else {
            errDiv.innerText = 'Errore imprevisto nella ricerca del dominio.';
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
    const scadenza = new Date(dominio.lastContract?.expirationDate + 'T00:00:00');
   
    const cell = riga.insertCell();

    if (oggi <= scadenza) {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.innerText = "Rinnova";
        btn.addEventListener("click", () => {
            // Reindirizza alla pagina di acquisto in modalità "rinnovo"
            window.location.href = `acquisto.html?dominio=${encodeURIComponent(dominio.name)}&tipo=rinnovo`;
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
    riga.insertCell().innerText = formatDate(ordine.date); // L'API non sembra avere 'timestamp' ma lo assumiamo
    riga.insertCell().innerText = ordine.type || 'N/A';
    riga.insertCell().innerText = ordine.cost ? `${ordine.cost.toFixed(2)} €` : 'N/A';
}

// === 6. Inizializzazione ===
async function init() {
    // Recupera l'ID utente dal localStorage
    const userId = localStorage.getItem('sessionId');

    // Se non c'è un utente loggato, reindirizza al login
    if (!userId) {
        window.location.href = './auth/login.html';
        return; // Interrompe l'esecuzione
    }
    
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
            cercaDominio(e, userId); // Passa l'evento 'e' alla funzione
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