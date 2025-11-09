const API_URI = "http://localhost:8080";

window.onload = init();

async function unlockDominio(dominio, id)
{
    try {
        // Effettuare una richiesta GET all'API per rilasciare un dominio
        const response = await fetch(API_URI + '/dominio/unlock/' + dominio + '?id=' + id, {
            method: 'GET'
        });

        // Gestire la risposta dell'API
        if(response.status == 200)
        {
            console.log('Dominio liberato con successo');
        }
        else
        {
            console.error('Errore nel rilascio del dominio');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function rinnovaDominio(dominio, id)
{
    const anni = document.getElementById('durata').value; // Questo valore rappresenta gli anni
    // Creare un oggetto con i dati del modulo
    let data = {
      domainName: dominio,
      userId: parseInt(id, 10),
      requestAction: "ACQUIRED",
      yearDuration: parseInt(anni, 10), // Il backend si aspetta il numero di anni
      // I dati della carta non sono richiesti dall'endpoint, quindi non li inviamo.
      // Vengono inseriti solo per simulare un form completo.
    };
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = ''; // Pulisce messaggi precedenti

    try {
        // Effettuare una richiesta PUT all'API per rinnovare un dominio
        // L'endpoint corretto è /domains/{domainName}/renew
        const response = await fetch(API_URI + '/domains/new', {
            method: 'POST', // POST per creare una nuova registrazione
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        const responseData = await response.json().catch(() => ({}));

        // Gestire la risposta dell'API
        if(response.ok && responseData.status === 'REGISTERED')
        {
            acquistoCompletato = true; // Imposta il flag a true
            // ✅ Messaggio di successo in verde
            resultDiv.innerHTML = `<p class="success-message">Dominio ${dominio} acquistato con successo! Sarai reindirizzato...</p>`;
            // Reindirizza alla pagina dei domini dopo 2 secondi
            setTimeout(() => {
                window.location.href = `index.html`; // L'id utente è già nel localStorage
            }, 2000);
        }
        else {
            // ❌ Messaggio di errore in rosso
            let errorMessage = "Errore imprevisto durante l'acquisto del dominio.";
            resultDiv.innerHTML = `<p class="error-message">${errorMessage}</p>`;
        }
    } catch (error) {
        console.error('Error:', error);
        resultDiv.innerHTML = `<p class="error-message">Errore di connessione al server. Riprova più tardi.</p>`;
    }
}

async function init() 
{
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
        // Stampare il dominio da acquistare sulla pagina
        document.getElementById("dominio").innerText = "Dominio da acquistare: " + dominio;
    if(tipo == "rinnovo")
        // Stampare il dominio da rinnovare sulla pagina
        document.getElementById("dominio").innerText = "Dominio da rinnovare: " + dominio;

    // Aggiungere un ascoltatore per il modulo di acquisto
    document.getElementById("acquisto").addEventListener("submit", (event) => {
        event.preventDefault();
        if(tipo == "acquisto")
            // Acquista il dominio
            acquistaDominio(dominio, id);
        if(tipo == "rinnovo")
            // Rinnova il dominio
            rinnovaDominio(dominio, id);
    });

    // Aggiungere un ascoltatore per il link di ritorno
    document.getElementById("indietro").addEventListener("click", () => {
        if (tipo === 'acquisto') {
            unlockDominio(dominio, id);
        }
        window.location.href = "index.html";
    });

    // Aggiungere un ascoltatore per quando si chiude la pagina
    window.addEventListener('beforeunload', (event) => {
        // Rilascia il dominio solo se l'acquisto non è stato completato
        // e se siamo in modalità acquisto
        if (!acquistoCompletato && tipo === 'acquisto') {
            event.preventDefault(); // Necessario per alcune chiamate sincrone, anche se qui è asincrona
            unlockDominio(dominio, id);
        }
    });

    // Aggiungi gli stili CSS per i messaggi di successo/errore
    const style = document.createElement('style');
    style.textContent = `
        .success-message { color: green; font-weight: bold; }
        .error-message { color: red; font-weight: bold; }
    `;
    document.head.appendChild(style);
}