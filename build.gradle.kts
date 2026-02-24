import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.cloud.tools.jib") version "3.4.4"
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
}

group = "com.unblu.demo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	implementation("org.springframework.session:spring-session-core")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	implementation("com.nimbusds:nimbus-jose-jwt:10.8")
	implementation("org.bouncycastle:bcprov-jdk15on:[1.61,)")
	implementation("org.bouncycastle:bcpkix-jdk15on:[1.61,)")

	// WebJars
	implementation("org.webjars:webjars-locator-core:0.59")
	implementation("org.webjars:bootstrap:5.3.8")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	// Development
	developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property")
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	to {
		image = "ghcr.io/unblu/unblu-visitor-sso-sample"
	}
	container {
		mainClass = "com.unblu.demo.sample.jwt.ApplicationKt"
		ports = listOf("8080")
	}
}
