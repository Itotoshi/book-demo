plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "9.20.0"
    id("org.jooq.jooq-codegen-gradle") version "3.19.25"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.jooq:jooq:3.19.11")
    implementation("org.jooq:jooq-meta:3.19.11")
    implementation("org.jooq:jooq-codegen:3.19.11")
    implementation("org.jooq:jooq-postgres-extensions:3.19.11")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    jooqCodegen("org.postgresql:postgresql:42.7.4")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("io.mockk:mockk:1.13.5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

flyway {
    url = "jdbc:postgresql://localhost:5432/mydatabase"
    user = "myuser"
    password = "secret"
    schemas = arrayOf("public")
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:5432/mydatabase"
            user = "myuser"
            password = "secret"
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database{
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                includes = ".*"
            }
            target{
                packageName = "com.example.jooq"
                directory = "build/generated-src/jooq/main"
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated-src/jooq/main")
        }
    }
}