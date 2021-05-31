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
	var unbluUrl = `${unbluBaseUrl}/rest/v3/authenticator/loginWithSecureToken?x-unblu-apikey=${unbluApiKey}`;
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
			activateUnbluJwt(data.token, unbluUrl)
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
function activateUnbluJwt(jwt, unbluUrl) {
	// XMLHttpRequest is mandatory here, fetch ignores Set-Cookie headers
	var xhttp = new XMLHttpRequest();
	xhttp.withCredentials = true;
	return new Promise(function (resolve, reject) {
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4) {
				if (this.status == 204) {
					console.log('token activated', xhttp);
					resolve('done');
				} else {
					// console.error('unblu response: ', xhttp);
					reject('Failed to activate token!')
				}
			}
		};
		xhttp.open("POST", unbluUrl, true);
		xhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
		xhttp.send(JSON.stringify({token: jwt, type: 'JWT'}));
	});
}

function logout(unbluBaseUrl) {
    var unbluLogoutUrl = unbluBaseUrl + "/rest/v3/authenticator/logout";
	var xhttp = new XMLHttpRequest();
	xhttp.withCredentials = true;
	return new Promise(function (resolve, reject) {
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4) {
				if (this.status == 204) {
					console.log('logout successful', xhttp);
					resolve('done');
				} else {
					reject('Failed to stop session!')
				}
			}
		};
		xhttp.open("POST", unbluLogoutUrl, true);
		xhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
		xhttp.send(JSON.stringify({redirectOnSuccess: null}));
	});

}