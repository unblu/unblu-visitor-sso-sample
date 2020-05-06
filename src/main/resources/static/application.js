
function login(unbluUrl) {
	fetch('/api/token', {method: 'POST'})
		.then((response) => {
			return response.json();
		})
		.then((data) => {
			console.log('jwt: ', data);
			activateUnbluJwt(data.token, unbluUrl)
				.then((response) => {
					window.location.href = '/secure';
				})
				.catch((error) => {
					document.getElementById('login-result').textContent = 'Login failed! ' + error;
				});
		});
}

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

function logout(unbluLogoutUrl) {
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
		xhttp.open("GET", unbluLogoutUrl, true);
		xhttp.send();
	});

}