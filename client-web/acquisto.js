const API_URI = "http://localhost:8080";

let transazioneCompletata = false; // Flag per gestire l'unlock del dominio

/**
 * Esegue la transazione di acquisto o rinnovo di un dominio.
 * Le due operazioni usano lo stesso endpoint e la stessa logica.
 * @param {string} dominio - Il nome del dominio.
 * @param {string} userId - L'ID dell'utente.
 * @param {string} tipo - Il tipo di operazione ('acquisto' or 'rinnovo').
 */
async function eseguiTransazioneDominio(dominio, userId, tipo) {
    const anni = document.getElementById('durata').value;
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = ''; // Pulisce messaggi precedenti

    let endpoint = '';
    let method = '';
    let body = {};

    if (tipo === 'acquisto') {
        endpoint = `${API_URI}/domains/new`;
        method = 'POST';
        body = {
            domainName: dominio,
            userId: parseInt(userId, 10),
            requestAction: "ACQUIRED", // Finalizza l'acquisto
            yearDuration: parseInt(anni, 10),
        };
    } else if (tipo === 'rinnovo') {
        endpoint = `${API_URI}/domains/${encodeURIComponent(dominio)}/renew`;
        method = 'PUT';
        body = {
            userId: parseInt(userId, 10),
            yearsDuration: parseInt(anni, 10) // Come da documentazione REST.md
        };
    } else {
        resultDiv.innerHTML = `<p class="error-message">Tipo di operazione non valido.</p>`;
        return;
    }

    try {
        const response = await fetch(endpoint, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        });

        const responseData = await response.json().catch(() => ({})); // Gestisce risposte senza corpo JSON

        if (response.ok) { // Per il rinnovo lo status potrebbe non essere 'REGISTERED'
            transazioneCompletata = true; // Impedisce l'unlock del dominio in caso di chiusura pagina
            resultDiv.innerHTML = `<p class="success-message">Operazione per il dominio ${dominio} completata con successo! Sarai reindirizzato...</p>`;
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);
        } else {
            const errorMessage = responseData.error || "Errore imprevisto durante l'operazione.";
            resultDiv.innerHTML = `<p class="error-message">${errorMessage}</p>`;
        }
    } catch (error) {
        console.error('Errore di rete:', error);
        resultDiv.innerHTML = `<p class="error-message">Errore di connessione al server. Riprova più tardi.</p>`;
    }
}

/**
 * Invia una richiesta al server per sbloccare un dominio bloccato in fase di acquisto.
 * @param {string} dominio - Il nome del dominio da sbloccare.
 * @param {string} userId - L'ID dell'utente che ha bloccato il dominio.
 */
async function unlockDominio(dominio, userId) {
    try {
        // Un'operazione di sblocco dovrebbe modificare lo stato, quindi usiamo POST.
        await fetch(`${API_URI}/domains/unlock`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ domainName: dominio, userId: parseInt(userId, 10) })
        });
        console.log(`Richiesta di sblocco per il dominio ${dominio} inviata.`);
    } catch (error) {
        console.error('Errore durante lo sblocco del dominio:', error);
    }
}

async function init() 
{
    window.onload = init; // Correzione: assegna la funzione, non il suo risultato

    // Ottenere i parametri dalla query string
    const urlParams = new URLSearchParams(window.location.search);
    const dominio = urlParams.get('dominio');    
    const tipo = urlParams.get('tipo');

    // L'ID utente dovrebbe essere preso dal localStorage per coerenza e sicurezza
    const id = localStorage.getItem('sessionId');

    // Se non c'è un utente loggato, reindirizza al login
    if (!id) {
        window.location.href = './auth/login.html';
        return; // Interrompe l'esecuzione
    }

    // Stampare i parametri nella console
    console.log("ID Utente: " + id);
    console.log("Tipo: " + tipo);
    if(tipo == "acquisto")
        document.getElementById("dominio").innerText = "Dominio da acquistare: " + dominio;
    if(tipo == "rinnovo")
        document.getElementById("dominio").innerText = "Dominio da rinnovare: " + dominio;

    // Aggiungere un ascoltatore per il modulo di acquisto
    document.getElementById("acquisto").addEventListener("submit", (event) => {
        event.preventDefault();
        // La logica è la stessa sia per acquisto che per rinnovo
        eseguiTransazioneDominio(dominio, id, tipo);
    });

    // Aggiungere un ascoltatore per il link di ritorno
    document.getElementById("indietro").addEventListener("click", () => {
        if (tipo === 'acquisto' && !transazioneCompletata) {
            unlockDominio(dominio, id);
        }
        window.location.href = "index.html";
    });

    window.addEventListener('beforeunload', (event) => {
        // Rilascia il dominio solo se l'acquisto non è stato completato
        // e se siamo in modalità acquisto
        if (!acquistoCompletato && tipo === 'acquisto') {
            event.preventDefault(); // Necessario per alcune chiamate sincrone, anche se qui è asincrona
            unlockDominio(dominio, id);
        }
    });

    // Aggiunge stili CSS per i messaggi di feedback all'utente
    const style = document.createElement('style');
    style.textContent = `
        .success-message { color: green; font-weight: bold; }
        .error-message { color: red; font-weight: bold; }
    `;
    document.head.appendChild(style);
}

window.onload = init;