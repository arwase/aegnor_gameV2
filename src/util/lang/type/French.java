package util.lang.type;

import kernel.Config;
import util.lang.AbstractLang;

/**
 * Created by Locos on 09/12/2015.
 */
public class French extends AbstractLang {

    private final static French singleton = new French();

    public static French getInstance() {
        return singleton;
    }

    public void initialize() {
        int index = 0;
        //0
        this.sentences.add(index, "Votre canal général est désactivé."); index++;
        //1
        this.sentences.add(index, "Les caractères point virgule, chevrons et tildé sont désactivé."); index++;
        //2
        this.sentences.add(index, "Tu dois attendre encore #1 seconde(s)."); index++;
        //3
        this.sentences.add(index, "Vous avez activé le canal général."); index++;
        //4
        this.sentences.add(index, "Vous avez désactivé le canal général."); index++;
        //5
        this.sentences.add(index, "Liste des membres du staff connectés :"); index++;
        //6
        this.sentences.add(index, "Il n'y a aucun membre du staff connecté."); index++;
        //7
        this.sentences.add(index, "Vous n'êtes pas bloquer.."); index++;
        //8
        this.sentences.add(index, "<b>" + Config.INSTANCE.getNAME() + " - <a href='" + Config.INSTANCE.getUrl() + "'>Site</a></b>\nEn ligne depuis : #1j #2h #3m #4s."); index++;
        //9
        this.sentences.add(index, "\nJoueurs en ligne : #1"); index++;
        //10
        this.sentences.add(index, "\nJoueurs uniques en ligne : #1"); index++;
        //11
        this.sentences.add(index, "\nRecord de connexion : #1"); index++;
        //12
        this.sentences.add(index, "Les commandes disponibles sont :\n"
                + "<b>.infos</b> - Permet d'obtenir des informations sur le serveur.\n"
                + "<b>.deblo</b> - Permet de vous débloquer en vous téléportant à une cellule libre.\n"
                + "<b>.staff</b> - Permet de voir les membres du staff connectés.\n"
                + "<b>.all</b> - Permet d'envoyer un message à tous les joueurs.\n"
                + "<b>.noall</b> - Permet de ne plus recevoir les messages du canal général.\n" +
                "<b>.maitre</b> - Active le mode maitre sur le personnage indiqué, néccesite d'être le chef du groupe.\n" +
                (Config.INSTANCE.getTEAM_MATCH() ? "<b>.kolizeum</b> - Vous inscrit/désincrit de la liste d'attente de Kolizeum.\n" : "") +
                (Config.INSTANCE.getDEATH_MATCH() ? "<b>.deathmatch</b> - Vous inscrit/désincrit de la liste d'attente de DeathMatch.\n" : "") +
                "<b>.banque</b> - Ouvre votre banque.\n" +
                "<b>.transfert</b> - Nécessite d'être dans sa banque et permet de transférer toutes vos ressources.\n" +
                "<b>.groupe</b> - Groupe vos mules.\n" +
                "<b>.demorph</b> - Remet votre apparence normal.\n"); index++;
        //13
        this.sentences.add(index, "Vous pouvez dès à présent voter, <b><a href='" + Config.INSTANCE.getUrl() + "'>clique ici</a></b> !"); index++;
        //14
        this.sentences.add(index, "Vous ne pouvez plus combattre jusqu'à nouvelle ordre."); index++;
        //15
        this.sentences.add(index, "Vous pouvez désormais combattre."); index++;
        //16
        this.sentences.add(index,"Pour afficher les commandes disponibles faites :\n"
                + "<b>.commande</b> - Permet d'afficher les commandes standards.\n"
                + "<b>.commandemulti</b> - Permet d'afficher les commandes multicomptes.\n"
                + "<b>.commandevip</b> - Permet d'afficher les commandes VIP.\n"
                + "<b>.commandebuy</b> - Permet d'afficher les commandes payantes."); index++;
        //17
        this.sentences.add(index,"<b>***COMMANDES STANDARD***</b> disponibles sont :\n"
                + "<b>.infos</b> - Permet d'obtenir des informations sur le serveur.\n"
                + "<b>.deblo</b> - Permet de vous débloquer d'une case non marchable.\n"
                + "<b>.fightdeblo</b> - Permet de vous débloquer en combat (passe le tour).\n"
                + "<b>.astrub</b> - Permet de vous téléporter a Astrub.\n"
                + "<b>.incarnam</b> - Permet de vous téléporter a Incarnam.\n"
                + "<b>.mapXP</b> - Permet de vous téléporter a la map Xp Kani.\n"
                + "<b>.staff</b> - Permet de voir les membres du staff connectés.\n"
                + "<b>.save</b> - Permet de sauvegarder votre personnage.\n"
                + "<b>.all</b> - Permet d'envoyer un message à tous les joueurs.\n"
                + "<b>.noall</b> - Permet de ne plus recevoir les messages globaux.\n"
                + "<b>.restat</b> - Permet de vous restat.\n"
                + "<b>.notiems</b> - Permet d'empecher le drop d'objets.\n"
                + "<b>.spellforget</b> - Permet d'oublier un sort.\n"
                + "<b>.hdv</b> - Permet d'afficher un hdv global.\n"
                + "<b>.boutique</b> - Permet d'afficher les objets boutiques.\n"
                + "<b>.points</b> - Permet d'afficher tes points boutiques.\n"
                + "<b>.sellitem</b> - Vend tout votre inventaire excepté les dofus, items équipés et objet de quête."); index++;
        //18
        this.sentences.add(index,"<b>***COMMANDES MAITRE***</b> disponibles sont :\n"
                + "<b>.groupe</b> - Permet de grouper l'ensemble de tes persos.\n"
                + "<b>.tp</b> - Permet de téléporter tes persos sur ta map.\n"
                + "<b>.maitre</b> - Permet de faire suivre tes esclaves (combat,map,pret).\n"
                + "<b>.ipdrop</b> - Permet de récuperer les drops de tes esclaces.\n"
                + "<b>.getmaster</b> - Permet d'afficher qui est ton maitre.\n"
                + "<b>.getslave</b> - Permet d'afficher tes esclaves.\n"
                + "<b>.resetmaitre</b> - Si tu es maitre, libères tes esclaves.\n"
                + "<b>.pass</b> - Permet de passer ton tour automatiquement.\n"
                + "<b>.passall</b> - Permet de mettre en état passe-tour toutes ses esclaves."); index++;
        //19
        this.sentences.add(index,"<b>***COMMANDES VIP***</b> disponibles sont :\n"
                + "<b>.banque</b> - Permet d'ouvrir la banque.\n"
                + "<b>.refreshMobs</b> - Permet de rafraichir les monstre de la map.\n"
                + "<b>.zaap</b> - Permet d'ouvrir la liste des zaap."); index++;
        // + "<b>.zaap</b> - Permet d'ouvrir la banque."
        //20
        this.sentences.add(index,"<b>***COMMANDES PAYANTES***</b> disponibles sont :\n"
                + "<b>.parcho</b> - (100PB) Permet de te parcho 101 partout.\n"
                + "<b>.spellboost</b> - (50PB) Donne 15 points de sort.\n"
                + "<b>.vip</b> - (400PB) Permet de passer VIP.\n");
        //+ "<b>.objivants</b> - (100PB) Donne cape et coiffe objivants."
    }
}
