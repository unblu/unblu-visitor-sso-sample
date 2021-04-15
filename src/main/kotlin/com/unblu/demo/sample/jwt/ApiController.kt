package com.unblu.demo.sample.jwt

import com.nimbusds.jose.*
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
import java.util.*


@RestController
@RequestMapping("api")
class ApiController(
    private val jwtConfig: JwtConfiguration,
    unbluConfiguration: UnbluConfiguration
) {

    private val key: RSAKey = RSAKeyGenerator(2048)
        .keyUse(KeyUse.SIGNATURE)
        .keyID(UUID.randomUUID().toString())
        .generate()
    private val signer = RSASSASigner(key.toRSAPrivateKey())

    private val unbluPublicKey: RSAKey = readKey(unbluConfiguration.publicKey)
    private val encrypter: JWEEncrypter = RSAEncrypter(unbluPublicKey)


    @PostMapping("token")
    fun createJwt(@RequestBody request: JwtRequest): TokenResponse {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .keyID(key.keyID)
            .build()
        val payload = createJwtPayload(request)
        val signedJWT = SignedJWT(header, payload)

        signedJWT.sign(signer)

        val token = if (jwtConfig.encryption) {
            // Create JWE object with signed JWT as payload
            val jweHeader = JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT")
                .build()
            val jweObject = JWEObject(jweHeader, Payload(signedJWT))
            // Encrypt with the recipient's public key
            jweObject.encrypt(encrypter)
            jweObject.serialize()
        } else {
            signedJWT.serialize()
        }
        return TokenResponse(token)
    }

    private fun createJwtPayload(request: JwtRequest): JWTClaimsSet {
        val expiration = Date(System.currentTimeMillis() + jwtConfig.validFor.toMillis())
        return JWTClaimsSet.Builder()
            .issuer(jwtConfig.issuer)
            .audience(jwtConfig.audience)
            .issueTime(Date())
            .expirationTime(expiration)
            .claim("email", request.email)
            .claim("username", request.username)
            .claim("firstName", request.firstname)
            .claim("lastName", request.lastname)
            .build()
    }

    @GetMapping("jwk")
    fun keys(): Map<String, Any> {
        return JWKSet(key.toPublicJWK()).toJSONObject()
    }

    private fun readKey(key: Resource): RSAKey {
        val text = key.inputStream.bufferedReader().readText()
        return JWK.parseFromPEMEncodedObjects(text) as RSAKey
    }
}