/**
  * Check Unblu authentication and update UI state accordingly.
  */
async function initPage(unbluBaseUrl, unbluApiKey) {
    var authentication = document.getElementById('authentication');
    var signInForm = document.getElementById('sign-in-form');
    var signOutForm = document.getElementById('sign-out-form');
    var isAuthenticated = await checkAuthentication(unbluBaseUrl);
    if (isAuthenticated) {
        authentication.innerHTML = "You are authenticated.";
        signInForm.style.display = 'none';
        signOutForm.style.display = 'block';
        // add the Unblu script tag
        var script = document.createElement("script");
        script.type = "text/javascript";
        script.src = `${unbluBaseUrl}/visitor.js?x-unblu-apikey=${unbluApiKey}`;
        document.head.append(script);
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
async function checkAuthentication(unbluBaseUrl) {
	var options = { credentials: 'include' };
	return await fetch(unbluBaseUrl + '/rest/v3/authenticator/isAuthenticated', options)
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
function login(unbluBaseUrl, unbluApiKey) {
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
			console.log('jwt: ', data);
			activateUnbluJwt(data.token, unbluBaseUrl, unbluApiKey)
				.then((response) => {
					initPage(unbluBaseUrl, unbluApiKey);
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
function activateUnbluJwt(jwt, unbluBaseUrl, unbluApiKey) {
	var request = {
		method: 'POST',
		headers: {
		  'Content-Type': 'application/json;charset=UTF-8'
		},
		body: JSON.stringify({token: jwt, type: 'JWT'}),
		credentials: 'include'
	};
	var loginUrl = `${unbluBaseUrl}/rest/v3/authenticator/loginWithSecureToken?x-unblu-apikey=${unbluApiKey}`;
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
function logout(unbluBaseUrl) {
	var request = {
		method: 'POST',
		headers: {
		  'Content-Type': 'application/json;charset=UTF-8'
		},
		body: JSON.stringify({redirectOnSuccess: null}),
		credentials: 'include'
	};
	var logoutUrl = unbluBaseUrl + "/rest/v3/authenticator/logout";
	return fetch(logoutUrl, request)
		.then((response) => {
			if (response.ok) {
				console.log('Unblu logout successful');
			} else {
				throw new Error('Unblu logout failed!');
			}
		});
}