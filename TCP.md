# Progetto di Sistemi Distribuiti 2023-2024:

---
## Documentazione del Protocollo TCP del Database

Questo documento descrive il protocollo testuale per interagire con il server del database.

### Connessione e Modello di Comunicazione

1.  **Connessione**: Un client si connette al server del database sulla porta `3030`. Il server assegna un thread dedicato per gestire la connessione.
2.  **Richiesta (Query)**: Il client invia una sequenza di comandi testuali, uno per riga. La sequenza termina con il comando `COMMIT`.
3.  **Elaborazione**: Il server riceve i comandi, li assembla in una query e la esegue.
4.  **Risposta**: Il server invia una singola riga di testo come risposta, indicando il risultato (`[SUCCESS]`, `[ERROR]`, ecc.) e, in caso di successo, i dati richiesti in formato JSON.

---

### Sintassi dei Comandi

Una query è composta da uno o più comandi seguiti da `COMMIT`.

#### Comandi sulle Collezioni

Questi comandi agiscono sull'intera collezione (il file JSON).

*   **`CREATE`**: Crea una nuova collezione.
    ```
    CREATE "<collection-name>" ON-KEY "<key-field>"
    ```
*   **`SELECT`**: Seleziona una collezione per le operazioni successive.
    ```
    SELECT "<collection-name>"
    ```
*   **`DELETE`**: Elimina un'intera collezione.
    ```
    DELETE "<collection-name>"
    ```

#### Comandi sui Documenti

Questi comandi vengono eseguiti dopo un `SELECT` e agiscono sui documenti all'interno della collezione selezionata.

*   **`INSERT`**: Inserisce un nuovo documento.
    ```
    SELECT "users"
    INSERT {"id": 1, "name": "Mario", ...}
    ```
*   **`SEARCH`**: Cerca documenti in base a uno o più criteri.
    ```
    SELECT "domains"
    SEARCH "name" = "example.com"
    ```
*   **`REMOVE`**: Rimuove un documento tramite la sua chiave primaria.
    ```
    SELECT "users"
    REMOVE "1"
    ```

### Esempio di Query Completa

Per trovare un utente con una specifica email, il client invia la seguente sequenza di testo, terminata da `COMMIT`:

```
SELECT "users"
SEARCH "email" = "mario.rossi@example.com"
COMMIT
```