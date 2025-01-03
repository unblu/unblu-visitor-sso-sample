package com.unblu.demo.sample.jwt

import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.AESEncrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


@RestController
@RequestMapping("api")
class ApiController(
    private val jwtConfig: JwtConfiguration,
    private val unbluConfig: UnbluConfiguration
) {
    // tag::key[]
    private val key: RSAKey = RSAKeyGenerator(2048)
        .keyUse(KeyUse.SIGNATURE)
        .keyID(UUID.randomUUID().toString())
        .generate()
    private val signer = RSASSASigner(key.toRSAPrivateKey())
    // end::key[]

    private val encrypter: JWEEncrypter = getEncrypter()


    @PostMapping("token")
    fun createJwt(@RequestBody userInfo: JwtRequest, session: WebSession): TokenResponse {
        // tag::jwt[]
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(key.keyID)
            .build()
        val expiration = Date(System.currentTimeMillis() + jwtConfig.validFor.toMillis())
        val payload = JWTClaimsSet.Builder()
            .issuer(jwtConfig.issuer)
            .audience(jwtConfig.audience)
            .issueTime(Date())
            .expirationTime(expiration) // <1>
            .claim("email", userInfo.email)
            .claim("username", userInfo.username)
            .claim("firstName", userInfo.firstname)
            .claim("lastName", userInfo.lastname)
            .claim("logoutToken", session.id)
            .build()
        val signedJWT = SignedJWT(header, payload)

        signedJWT.sign(signer)

        val jwt: String = createJwt(signedJWT)
        // end::jwt[]

        session.start()

        return TokenResponse(jwt)
    }

    // tag::logout[]
    @GetMapping("logout")
    fun logout(session: WebSession): String {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(key.keyID)
            .build()
        val expiration = Date(System.currentTimeMillis() + jwtConfig.validFor.toMillis())
        val payload = JWTClaimsSet.Builder()
            .issuer(jwtConfig.issuer)
            .audience(jwtConfig.audience)
            .issueTime(Date())
            .expirationTime(expiration) // <1>
            .claim("logoutToken", session.id)
            .build()
        val signedJWT = SignedJWT(header, payload)

        signedJWT.sign(signer)
        val jwt: String = createJwt(signedJWT)

        val targetURI =
            URI.create(unbluConfig.serverUrl + unbluConfig.entryPath + "/rest/v3/authenticator/logoutWithSecureToken?x-unblu-apikey=" + unbluConfig.apiKey)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(targetURI)
            .POST(HttpRequest.BodyPublishers.ofString("{\n\"token\":\"$jwt\",\n\"type\":\"JWT\"\n}"))
            .header("Content-Type", "application/json;charset=UTF-8")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        session.invalidate()
            .subscribe()

        return response.body()
    }
    // end::logout[]

    // tag::jwk[]
    @GetMapping("jwk")
    fun keys(): Map<String, Any> {
        return JWKSet(key.toPublicJWK()).toJSONObject()
    }
    // end::jwk[]

    private fun getEncrypter(): JWEEncrypter {
        return when (jwtConfig.encryptionAlgorithm) {
            JWTEncryptionAlgorithm.RSA -> {
                val unbluPublicKey: RSAKey = readKey(unbluConfig.publicKey)
                RSAEncrypter(unbluPublicKey)
            }

            JWTEncryptionAlgorithm.AES -> {
                val encryptionKey = Base64.getDecoder().decode(unbluConfig.aesEncryptionKey)
                AESEncrypter(encryptionKey)
            }
        }
    }

    private fun readKey(key: Resource): RSAKey {
        val text = key.inputStream.bufferedReader().readText()
        return JWK.parseFromPEMEncodedObjects(text) as RSAKey
    }

    private fun createJwt(signedJWT: SignedJWT): String {
        return when (jwtConfig.encryption) {
            true -> encryptToken(signedJWT)
            false -> signedJWT.serialize()
        }
    }

    private fun encryptToken(signedJWT: SignedJWT): String {
        // Create JWE object with signed JWT as payload
        val algorithm: JWEAlgorithm = getEncryptionAlgorithm()
        val jweHeader = JWEHeader.Builder(algorithm, EncryptionMethod.A256GCM)
            .contentType("JWT")
            .build()
        val jweObject = JWEObject(jweHeader, Payload(signedJWT))
        // Encrypt with the recipient's public key
        jweObject.encrypt(encrypter)
        return jweObject.serialize()
    }

    private fun getEncryptionAlgorithm(): JWEAlgorithm {
        return when (jwtConfig.encryptionAlgorithm) {
            JWTEncryptionAlgorithm.RSA -> JWEAlgorithm.RSA_OAEP_256
            JWTEncryptionAlgorithm.AES -> JWEAlgorithm.A256KW
        }
    }
}