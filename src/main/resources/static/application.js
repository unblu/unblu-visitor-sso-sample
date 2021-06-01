class SampleApp {

    constructor(unbluServerUrl, unbluEntryPath, unbluApiKey) {
        this.unbluServerUrl = unbluServerUrl;
        this.unbluEntryPath = unbluEntryPath;
        this.unbluBaseUrl = unbluServerUrl + unbluEntryPath;
        this.unbluApiKey = unbluApiKey;
    }

    /**
      * Check Unblu authentication and update UI state accordingly.
      */
    async init() {
        var authentication = document.getElementById('authentication');
        var signInForm = document.getElementById('sign-in-form');
        var signOutForm = document.getElementById('sign-out-form');
        var isAuthenticated = await this.checkAuthentication(this.unbluBaseUrl);
        if (isAuthenticated) {
            authentication.innerHTML = "You are authenticated.";
            signInForm.style.display = 'none';
            signOutForm.style.display = 'block';
            // Initialize Unblu
            const unbluAPI = window.unblu.api.configure({
                serverUrl: this.unbluServerUrl,
                entryPath: this.unbluEntryPath,
                apiKey: this.unbluApiKey,
                locale: "en"
            }).initialize().then(console.log("Unblu JavaScript API is initialized."));
        } else {
            authentication.innerHTML = "You are not authenticated.";
            signInForm.style.display = 'block';
            signOutForm.style.display = 'none';
        }
    }

    /**
      * Calls the authentication verification endpoint of Unblu.
      *
      * @returns {Promise<boolean>} Whether the user is authenticated
      */
    async checkAuthentication() {
        var options = { credentials: 'include' };
        return await fetch(this.unbluBaseUrl + '/rest/v3/authenticator/isAuthenticated', options)
            .then(response => {
                if (!response.ok) {
                    const message = `An error has occurred: ${response.status}`;
                    throw new Error(message);
                }
                return response;
            })
            .then(response => response.json());
    }

    /**
      * Reads user information from the form, requests a JWT, and starts the authentication procedure of Unblu.
      * @see activateUnbluJwt
      */
    login() {
        var tokenRequest = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            firstname: document.getElementById('firstname').value,
            lastname: document.getElementById('lastname').value
        };
        var request = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(tokenRequest)
        };
        fetch('/api/token', request)
            .then((response) => {
                return response.json();
            })
            .then((data) => {
                console.log('Using JWT to log in: ', data);
                this.activateUnbluJwt(data.token)
                    .then((response) => {
                        this.init();
                    })
                    .catch((error) => {
                        document.getElementById('login-result').textContent = 'Login failed! ' + error;
                    });
            });
    }

    /**
      * Starts an Unblu authentication session using a JWT.
      * @returns {Promise}, fulfilled when login succeeded, rejected when login failed.
      */
    activateUnbluJwt(jwt) {
        var request = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            },
            body: JSON.stringify({ token: jwt, type: 'JWT' }),
            credentials: 'include'
        };
        var loginUrl = `${this.unbluBaseUrl}/rest/v3/authenticator/loginWithSecureToken?x-unblu-apikey=${this.unbluApiKey}`;
        return fetch(loginUrl, request)
            .then((response) => {
                if (response.ok) {
                    console.log('Unblu session activated');
                } else {
                    throw new Error('Failed to activate token!');
                }
            });
    }

    /**
      * Calls the Unblu logout endpoint.
      * @returns {Promise}, fulfilled when logout succeeded, rejected when logout failed.
      */
    logout() {
        var request = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            },
            body: JSON.stringify({ redirectOnSuccess: null }),
            credentials: 'include'
        };
        var logoutUrl = this.unbluBaseUrl + "/rest/v3/authenticator/logout";
        return fetch(logoutUrl, request)
            .then((response) => {
                if (response.ok) {
                    console.log('Unblu logout successful');
                    location.reload();
                } else {
                    console.log('Logout failed', error);
                }
            });
    }
}