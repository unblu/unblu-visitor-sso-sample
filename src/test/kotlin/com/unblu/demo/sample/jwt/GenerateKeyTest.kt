package com.unblu.demo.sample.jwt

import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

class GenerateKeyTest {

    private val log = LoggerFactory.getLogger(GenerateKeyTest::class.java)

    @Test
    fun generateKeyPair() {
        val jwk: RSAKey = RSAKeyGenerator(2048)
                .keyUse(KeyUse.ENCRYPTION)
                .keyID(UUID.randomUUID().toString())
                .generate()
        log.info("Public key: {}", jwk.toPublicJWK().toJSONString())
        //log.info("Private key: {}", jwk.toPrivateKey().)
    }

}