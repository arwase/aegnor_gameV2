package kernel

object Config {

    val startTime = System.currentTimeMillis()

    var LOG = ConfigReader.data[ConfigReader.mode.log]
    var AUTO_CLEAN = ConfigReader.data[ConfigReader.mode.autoClean]
    var AZURIOM = ConfigReader.data[ConfigReader.mode.azuriom]
    var LINUX = ConfigReader.data[ConfigReader.mode.linux]
    var HALLOWEEN = ConfigReader.data[ConfigReader.mode.halloween]
    var NOEL = ConfigReader.data[ConfigReader.mode.christmas]
    var HEROIC = ConfigReader.data[ConfigReader.mode.heroic]
    var TEAM_MATCH = ConfigReader.data[ConfigReader.options.teamMatch]
    var DEATH_MATCH = ConfigReader.data[ConfigReader.options.deathMatch]
    var AUTO_EVENT = ConfigReader.data[ConfigReader.options.event.active]
    var AUTO_REBOOT = ConfigReader.data[ConfigReader.options.autoReboot]
    var ALL_ZAAP = ConfigReader.data[ConfigReader.options.allZaap]
    var ALL_EMOTE = ConfigReader.data[ConfigReader.options.allEmote]

    var isSaving = false
    var isRunning = false

    var ENCRYPT_PACKET = ConfigReader.data[ConfigReader.options.encryptPacket]
    var TIME_PER_EVENT: Short = ConfigReader.data[ConfigReader.options.event.timePerEvent].toShort()

    var NAME: String = "Aegnor"
    var url: String = "https://aegnor.arwase.fr"
    var startMessage = "Bienvenue sur le serveur $NAME !"
    var colorMessage = "B9121B"

    var START_MAP = ConfigReader.data[ConfigReader.options.start.map]
    var START_CELL = ConfigReader.data[ConfigReader.options.start.cell]
    var RATE_KAMAS = ConfigReader.data[ConfigReader.rate.kamas]
    var RATE_DROP = ConfigReader.data[ConfigReader.rate.farm]
    var RATE_HONOR = ConfigReader.data[ConfigReader.rate.honor]
    var RATE_JOB = ConfigReader.data[ConfigReader.rate.job]
    var RATE_XP = ConfigReader.data[ConfigReader.rate.xp]
    var RATE_FM = ConfigReader.data[ConfigReader.rate.fm]
    var PERCENT_EXO = ConfigReader.data[ConfigReader.rate.percent_exo]

    var PRIX_CHANGEMENT_CLASSE = ConfigReader.data[ConfigReader.prix.prix_changement_classe]
    var PRIX_CHANGEMENT_COULEUR = ConfigReader.data[ConfigReader.prix.prix_changement_couleur]
    var PRIX_CHANGEMENT_PSEUDO = ConfigReader.data[ConfigReader.prix.prix_changement_pseudo]
    var PRIX_MIMIBIOTE = ConfigReader.data[ConfigReader.prix.prix_mimibiote]

    var exchangePort: Int = ConfigReader.data[ConfigReader.exchange.port]
    var gamePort: Int = ConfigReader.data[ConfigReader.server.port]
    var exchangeIp: String = ConfigReader.data[ConfigReader.exchange.host]
    var loginHostDB: String = ConfigReader.data[ConfigReader.database.login.host]
    var loginPortDB: Int = ConfigReader.data[ConfigReader.database.login.port]
    var loginNameDB: String = ConfigReader.data[ConfigReader.database.login.name]
    var loginUserDB: String = ConfigReader.data[ConfigReader.database.login.user]
    var loginPassDB: String = ConfigReader.data[ConfigReader.database.login.pass]

    var siteHostDB: String = ConfigReader.data[ConfigReader.database.site.host]
    var sitePortDB: Int = ConfigReader.data[ConfigReader.database.site.port]
    var siteNameDB: String = ConfigReader.data[ConfigReader.database.site.name]
    var siteUserDB: String = ConfigReader.data[ConfigReader.database.site.user]
    var sitePassDB: String = ConfigReader.data[ConfigReader.database.site.pass]

    var hostDB: String? = ConfigReader.data[ConfigReader.database.game.host]
    var portDB: Int = ConfigReader.data[ConfigReader.database.game.port]
    var nameDB: String? = ConfigReader.data[ConfigReader.database.game.name]
    var userDB: String? = ConfigReader.data[ConfigReader.database.game.user]
    var passDB: String? = ConfigReader.data[ConfigReader.database.game.pass]
    var ip: String? = ConfigReader.data[ConfigReader.server.host]

    var SERVER_ID: Int = ConfigReader.data[ConfigReader.server.id]
    var SERVER_KEY: String = ConfigReader.data[ConfigReader.server.key]

    var subscription = ConfigReader.data[ConfigReader.options.subscription]

    var startKamas: Long = ConfigReader.data[ConfigReader.options.start.kamas]
    var startLevel: Int = ConfigReader.data[ConfigReader.options.start.level]



}