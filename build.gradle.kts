import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"

    id("com.gradleup.shadow") version "8.3.5"

    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0" // Generates plugin.yml based on the Gradle config
}

group = "dev.lizainslie"
version = "0.1.1"

val minecraftVersion = "1.21.10"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    
    maven("https://mvn.devos.one/releases") {
        name = "devOS"
    }
    
    maven("https://nexus.scarsz.me/content/groups/public/") {
        name = "scarsz"
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "placeholderapi"
    }

    maven("https://jitpack.io")

}

val exposedVersion: String by project

dependencies {
    // platform
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
//    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // orm
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-migration:$exposedVersion")
    
    // drivers
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("com.zaxxer:HikariCP:6.2.1") // connection pooling
    
    // integration
    compileOnly("com.github.NEZNAMY:TAB-API:5.3.2")
    compileOnly("com.discordsrv:discordsrv:1.28.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0") // packets :agony:
    compileOnly("me.clip:placeholderapi:2.11.6")
    
    // http
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

configurations.configureEach {
    resolutionStrategy.capabilitiesResolution.withCapability("org.bukkit:bukkit") {
        selectHighestVersion()
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn(tasks.reobfJar)
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

bukkitPluginYaml {
    main = "dev.lizainslie.cafemc.CafeMC"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    apiVersion = "1.21.10"
    softDepend = listOf("TAB", "Vault", "PlaceholderAPI")
    loadBefore = listOf("Vault")
    depend = listOf("ProtocolLib")
    
    authors = listOf("LizzyTheWitch")
    
    permissions {
        register("cafe.tpa.use") {
            description = "Teleport to a player & accept/deny requests"
        }

        register("cafe.tpa.back") {
            description = "Teleport back to your previous location"
        }

        register("cafe.afk") {
            description = "Toggle AFK status"
        }

        register("cafe.rename") {
            description = "Rename the item in your hand"
        }
        
        register("cafe.home") {
            description = "Teleport to & set your home"
        }
        
        register("cafe.balance") {
            description = "Check your balance"
        }
        
        register("cafe.balance.others") {
            description = "Check another player's balance"
        }
        
        register("cafe.pay") {
            description = "Pay another player"
        }
        
        register("cafe.deposit") {
            description = "Deposit your valuables as money into your account"
        }
        
        register("cafe.lock") {
            description = "Lock a block"
        }
        
        register("cafe.lock.bypass") {
            description = "Bypass locked blocks"
        }
        
        register("cafe.audit") {
            description = "Audit logged incidents that you have clearance to see"
        }
        
        register("cafe.audit.admin") {
            description = "Audit all logged incidents"
        }

        register("cafe.elytra") {
            description = "Toggle someones Elytra"
        }

        register("cafe.nickname") {
            description = "Set your nickname"
        }
    }
    
    commands {
        register("tpa") {
            description = "Request to teleport to a player"
            permission = "cafe.tpa.use"
            usage = "/<command> <player>"
        }
        
        register("tpaccept") {
            description = "Accept a teleport request"
            permission = "cafe.tpa.use"
            usage = "/<command> [player]"
        }
        
        register("tpdeny") {
            description = "Deny a teleport request"
            permission = "cafe.tpa.use"
            usage = "/<command> [player]"
        }
        
        register("back") {
            description = "Teleport back to your previous location"
            permission = "cafe.tpa.back"
            usage = "/<command>"
        }
        
        register("afk") {
            description = "Toggle AFK status"
            permission = "cafe.afk"
            usage = "/<command>"
        }
        
        register("rename") {
            description = "Rename the item in your hand"
            permission = "cafe.rename"
            usage = "/<command> <name>"
        }
        
        register("home") {
            description = "Teleport to & set your home"
            permission = "cafe.home"
            usage = "/<command> [set|clear]"
        }
        
        register("migrate") {
            description = "Migrate database changes"
        }
        
        register("balance") {
            description = "Check your balance or the balance of another player"
            permission = "cafe.balance"
            usage = "/<command> [player]"
            aliases = listOf("bal")
        }
        
        register("pay") {
            description = "Pay another player"
            permission = "cafe.pay"
            usage = "/<command> <player> <amount>"
        }
        
        register("deposit") {
            description = "Deposit your valuables as money into your account"
            permission = "cafe.deposit"
            usage = "/<command> [open]"
        }
        
        register("lock") {
            description = "Lock a block"
            permission = "cafe.lock"
            usage = "/<command>"
        }
        
        register("unlock") {
            description = "Unlock a block"
            permission = "cafe.lock"
            usage = "/<command>"
        }
        
        register("audit") {
            description = "Audit logged incidents"
            permission = "cafe.audit"
            usage = "/<command> <incident_type|all> <get|list|find> [id|param=value] [param=value]..."
        }
        
        register("testcomponent") {
            description = "Test the component system"
            usage = "/<command> [runcommandtest]"
        }

        register("nickname") {
            description = "Set or reset your nickname"
            permission = "cafe.nickname"
            usage = "/<command> [nickname]"
            aliases = listOf("nick")
        }

        register("elytra") {
            description = "Toggle someones Elytra"
            usage = "/<command> <player>"
        }
    }
}
