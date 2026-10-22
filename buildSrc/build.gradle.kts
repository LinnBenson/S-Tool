plugins { `java-library` }
repositories { google(); mavenCentral() }
dependencies { implementation("com.android.tools.build:gradle-api:8.7.3"); implementation("org.ow2.asm:asm:9.7") }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
