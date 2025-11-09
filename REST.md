# Progetto di Sistemi Distribuiti 2023-2024:

---

### Descrizione:
Documentazione delle API RESTfull del progetto Registra Domini, fornite dal server web.
Ogni risorsa (domains, users, operations) fornita dal server ha un path dedicato.

---

### `./domains`

> #### POST `./domains/new`
> - **Descrizione**: Permette di riservare un dominio per l'acquisto di una registrazione o di completare l'acquisto di una registrazione già in corso.
> - **Parametri Path**: `-`
> - **Parametri Query**: `-`
> - **Body**:
	`
	{
		"domainName":"sementi-denditriche.com",
		"requestAction":"ACQUIRED",
		"userId":30,
		"yearDuration":1
	}
	`
> - **Response Status Code**:
	- 200: OK
	- 400: Bad Request
	- 403: Forbidden
	- 409: Conflict
> - **Response**: In caso di successo restituisce la rappresentazione JSON della struttura dati DomainInfo associata al dominio creato/modificato. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

> #### GET `./domains/{domainName}`
> - **Descrizione**: Permette di ottenere le informazioni di un dominio.
> - **Parametri Path**: `domainName`
> - **Parametri Query**: `-`
> - **Body**: `-`
> - **Response Status Code**:
	- 200: OK
	- 404: Not Found
> - **Response**: In caso di successo restituisce la rappresentazione JSON della struttura dati DomainInfo associata al dominio richiesto. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

> #### PUT `./domains/{domainName}/renew`
> - **Descrizione**: Permette di rinnovare un dominio attualmene registrato.
> - **Parametri Path**: `domainName`
> - **Parametri Query**: `-`
> - **Body**:
	`
	{
		"userId":30,
		"yearsDuration":1
	}
	`
> - **Response Status Code**:
	- 200: OK
	- 400: Bad Request
	- 404: Not Found
> - **Response**: In caso di successo restituisce la rappresentazione JSON della struttura dati DomainInfo associata al dominio rinnovato. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

<br>

### `./users`

> #### POST `./users/signin`
> - **Descrizione**: Permette di effettuare l'accesso ad un account precedentemente registrato.
> - **Parametri Path**: `-`
> - **Parametri Query**: `-`
> - **Body**:
	`
	{
		"email":"sementi@denditri.che",
		"password":"sementi-denditriche-30"
	}
	`
> - **Response Status Code**:
	- 200: OK
	- 400: Bad Request
	- 404: Not Found
	- 500: Internal Server Error
> - **Response**: In caso di successo restituisce la rappresentazione JSON della struttura dati UserInfo associata all'utente che ha effettuato l'accesso. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

> #### POST `./users/signup`
> - **Descrizione**: Permette di registrare un nuovo account utente.
> - **Parametri Path**: `-`
> - **Parametri Query**: `-`
> - **Body**:
	`
	{
		"name":"sementi",
		"surname":"denditriche",
		"email":"sementi@denditri.che",
		"password":"sementi-denditriche-30"
	}
	`
> - **Response Status Code**:
	- 200: OK
	- 400: Bad Request
	- 409: Conflict
	- 500: Internal Server Error
> - **Response**:  In caso di successo restituisce la rappresentazione JSON della struttura dati UserInfo associata al nuovo utente registrato. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

> #### GET `./users/{userId}`
> - **Descrizione**: Permette di ottenere le informazioni di un account utente precedentemente registrato.
> - **Parametri Path**: `userId`
> - **Parametri Query**: `-`
> - **Body**: `-`
> - **Response Status Code**:
	- 200: OK
	- 404: Not Found
> - **Response**:  In caso di successo restituisce la rappresentazione JSON della struttura dati UserInfo associata all'utente richiesto. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

> #### GET `./users/{userId}/domains`
> - **Descrizione**: Permette di ottenere la lista dei domini di un utente (registrati e scaduti non ancora registrati).
> - **Parametri Path**: `userId`
> - **Parametri Query**: `-`
> - **Body**: `-`
> - **Response Status Code**:
	- 200: OK
	- 404: Not Found
> - **Response**:  In caso di successo restituisce la rappresentazione JSON della lista di domini attualmente registrati dall'utente oppure scaduti e ancora non registrati da nessuno. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.

<br>

### `./operations`

> #### GET `./operations[?<userId="userId">[&domainName="domainName"][&operationType="operationType"]]`
> - **Descrizione**: Permette di ottenere una lista di operazioni svolte nel sistema filtrata per parametri opzionali.
> - **Parametri Path**: `-`
> - **Parametri Query**: `[userId][domainName][operationType]`
> - **Body**: `-`
> - **Response Status Code**:
	- 200: OK
	- 404: Not Found
> - **Response**: In caso di successo restituisce la rappresentazione JSON della lista filtrata di operazioni. In caso di fallimento restituisce la rappresentazione JSON del messaggio di errore.