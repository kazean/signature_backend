# Ch02. Spring과 Kotlin
# Ch02-01. Spring에서 Kotlin 적용하기
- Java > Koltin
- build.gradle
```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.7.21'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
    testImplementation 'org.jetbrains.kotlin:kotlin-test'
}

test {
    useJUnitPlatform()
}

compileKotlin {
    kotlinOptions.jvmTarget = '11'
}

compileTestKotlin {
    kotlinOptions.jvmTarget = '11'
}
```
> plugins, compileKotlin, compileTestKotlin, dependencies