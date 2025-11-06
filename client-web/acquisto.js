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
        else if(response.status == 404)
        {
            console.error('Il dominio non era bloccato');
        }
        else if(response.status == 400)
        {
            console.error('Errore nei parametri');
        }
        else
        {
            console.error('Errore nel rilascio del dominio');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function acquistaDominio(dominio, id)
{
    const anni = document.getElementById('durata').value;
    // Creare un oggetto con i dati del modulo
    let data = {
      domainName: dominio,
      userId: parseInt(id, 10),
      requestAction: "ACQUIRED", // Conferma l'acquisto
      monthsDuration: parseInt(anni, 10) * 12,
      // I dati della carta non sono richiesti dall'endpoint, quindi non li inviamo.
      // Vengono inseriti solo per simulare un form completo.
    };

    try {
        // Effettuare una richiesta POST all'API per acquistare un dominio
        // L'endpoint corretto è /domains/new come da documentazione REST.md
        const response = await fetch(API_URI + '/domains/new', {
            method: 'POST', // POST per creare una nuova registrazione
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        // Gestire la risposta dell'API
        if(response.status == 200 || response.status == 201) // 200 OK o 201 Created
        {
            console.log('Dominio acquistato con successo');
            window.location.href = "domini.html?id=" + id;
        }
        else if(response.status == 403)
        {
            console.error('Limite di anni superato');
            document.getElementById('err').innerText = 'Il limite massimo di anni è 10';
        }
        else if(response.status == 409)
        {
            console.error('Dominio esistente non scaduto oppure già bloccato');
            document.getElementById('err').innerText = 'Il dominio è già stato bloccato o non è ancora scaduto';
        }
        else if(response.status == 400)
        {
            console.error('Errore nel formato dei parametri o nel body');
            document.getElementById('err').innerText = 'Errore nei dati inseriti';
        }
        else
        {
            console.error('Errore nell\'acquisto del dominio');
            document.getElementById('err').innerText = 'Errore nell\'acquisto del dominio';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function rinnovaDominio(dominio, id)
{
    const anni = document.getElementById('durata').value;
    // Creare un oggetto con i dati del modulo
    let data = {
        userId: parseInt(id, 10),
        monthsDuration: parseInt(anni, 10) * 12
        // I dati della carta non sono richiesti dall'endpoint
    };

    try {
        // Effettuare una richiesta PUT all'API per rinnovare un dominio
        // L'endpoint corretto è /domains/{domainName}/renew
        const response = await fetch(`${API_URI}/domains/${encodeURIComponent(dominio)}/renew`, {
            method: 'PUT', // PUT per aggiornare (rinnovare)
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        // Gestire la risposta dell'API
        if(response.status == 200 || response.status == 201)
        {
            console.log('Dominio rinnovato con successo');
            window.location.href = "domini.html?id=" + id;
        }
        else if(response.status == 403)
        {
            console.error('Limite di anni superato');
            document.getElementById('err').innerText = 'Il limite massimo complessivo degli anni è 10 e il  minimo è 1';
        }
        else if(response.status == 404)
        {
            console.error('Dominio non trovato');
            document.getElementById('err').innerText = 'Il dominio non esiste o è scaduto';
        }
        else if(response.status == 400)
        {
            console.error('Errore nel formato dei parametri o nel body');
            document.getElementById('err').innerText = 'Errore nei dati inseriti';
        }
        else
        {
            console.error('Errore nel rinnovo del dominio');
            document.getElementById('err').innerText = 'Errore nel rinnovo del dominio';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function init() 
{
    // Ottenere i parametri dalla query string
    const urlParams = new URLSearchParams(window.location.search);
    const dominio = urlParams.get('dominio');
    const id = urlParams.get('id');
    const tipo = urlParams.get('tipo');
    // Stampare i parametri nella console
    console.log("Dominio: " + dominio);
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
        unlockDominio(dominio, id);
        window.location.href = "domini.html?id=" + id;
    });

    // Aggiungere un ascoltatore per quando si chiude la pagina
    window.addEventListener('beforeunload', (event) => {
        // Prima di chiudere la pagina, rilascia il dominio
        event.preventDefault();
        unlockDominio(dominio, id);
    });
}