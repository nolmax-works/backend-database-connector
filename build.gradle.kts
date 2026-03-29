plugins {
    id("java")
    id("maven-publish")
}

group = "com.nolmax.database"
version = "1.3.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.postgresql:postgresql:42.7.10")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("nolmax's backend database connector")
                description.set("database utility library for nolmax")
                url.set("https://github.com/nolmax-works/backend-database-connector")
            }
        }
    }

    repositories {
        maven {
            name = "qtpcRepo"
            url = uri("https://maven.qtpc.tech/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}