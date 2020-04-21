package com.unblu.demo.sample.jwt

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import net.minidev.json.JSONObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.UUID

@RestController
@RequestMapping("api")
class ApiController(private val jwtConfig: JwtConfiguration) {

    private val key: RSAKey = RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .generate()
    private val signer = RSASSASigner(key.toRSAPrivateKey())

    @PostMapping("token")
    fun createJwt(): TokenResponse {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(key.keyID)
                .build()

        val expiration = Date(System.currentTimeMillis() + jwtConfig.validFor.toMillis())
        val payload = JWTClaimsSet.Builder()
                .issuer(jwtConfig.issuer)
                .audience(jwtConfig.audience)
                .issueTime(Date())
                .expirationTime(expiration)
                // TODO use values from request parameters
                .claim("email", "john.doe@bar.com")
                .claim("firstName", "John")
                .claim("lastName", "Doe")
                .build()
        val signedJWT = SignedJWT(header, payload)
        signedJWT.sign(signer)
        return TokenResponse(signedJWT.serialize())
    }

    @GetMapping("jwk")
    fun keys(): JSONObject {
        return JWKSet(key.toPublicJWK()).toJSONObject()
    }

}