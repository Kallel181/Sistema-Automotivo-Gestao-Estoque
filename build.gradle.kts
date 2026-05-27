plugins {
    id("java")
    // Atualize a versão do Spring Boot para a 3.4.x ou 3.5.x (ex: 3.4.2 ou superior)
    id("org.springframework.boot") version "3.4.2"
    // Atualize a versão do gerenciamento de dependências
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.concessionaria"
version = "1.0-SNAPSHOT"

// CORREÇÃO: Força o compilador do Java 25 a gerar bytes compatíveis com Java 21
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j:8.0.33")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}