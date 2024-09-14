package kernel

import com.natpryce.konfig.*
import java.io.File

object ConfigReader {
    lateinit var data: Configuration

    init {
        this.reload()
    }

    fun reload() {
        this.data = EnvironmentVariables() overriding ConfigurationProperties.fromFile(File("config.properties"))
    }

    object server : PropertyGroup() {
        val id by intType
        val key by stringType
        val host by stringType
        val name by stringType
        val version by stringType
        val port by intType
    }

    object exchange : PropertyGroup() {
        val host by stringType
        val port by intType
    }

    object database : PropertyGroup() {
        object login : PropertyGroup() {
            val host by stringType
            val port by intType
            val user by stringType
            val pass by stringType
            val name by stringType
        }

        object game : PropertyGroup() {
            val host by stringType
            val port by intType
            val user by stringType
            val pass by stringType
            val name by stringType
        }

        object site : PropertyGroup() {
            val host by stringType
            val port by intType
            val user by stringType
            val pass by stringType
            val name by stringType
        }
    }

    object rate : PropertyGroup() {
        val fm by intType
        val percent_exo by intType
        val xp by doubleType
        val job by intType
        val farm by intType
        val honor by intType
        val kamas by intType
    }

    object prix : PropertyGroup() {
        val prix_changement_classe by intType
        val prix_changement_couleur by intType
        val prix_changement_pseudo by intType
        val prix_mimibiote by intType
    }

    object mode : PropertyGroup() {
        val log by booleanType
        val autoClean by booleanType
        val linux by booleanType
        val azuriom by booleanType
        val halloween by booleanType
        val christmas by booleanType
        val heroic by booleanType
        val discordBot by booleanType
        val discordWebhooks by booleanType
        val hdvGlobal by booleanType
    }

    object discord : PropertyGroup() {
        val key by stringType
        object channelId : PropertyGroup() {
            val log by stringType
            val command by stringType
            val faille by stringType
            val event by stringType
            val info by stringType
        }
    }

    object options : PropertyGroup() {
        object start : PropertyGroup() {
            val message by stringType
            val map by intType
            val cell by intType
            val kamas by longType
            val level by intType
        }
        object event : PropertyGroup() {
            val active by booleanType
            val timePerEvent by intType
        }

        val autoReboot by booleanType
        val encryptPacket by booleanType
        val deathMatch by booleanType
        val teamMatch by booleanType
        val allZaap by booleanType
        val allEmote by booleanType
        val subscription by booleanType
    }
}