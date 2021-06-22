package command;

import area.map.GameMap;
import area.map.entity.House;
import client.Player;
import client.other.Party;
import common.PathFinding;
import common.SocketManager;
import database.Database;
import event.EventManager;
import exchange.ExchangeClient;
import fight.arena.FightManager;
import fight.arena.TeamMatch;
import game.GameClient;
import game.GameServer;
import game.action.ExchangeAction;
import game.world.World;
import hdv.Hdv;
import kernel.Boutique;
import kernel.Config;
import kernel.Constant;
import kernel.Logging;
import object.GameObject;
import util.lang.Lang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandPlayer {

    public final static String canal = "Aegnor";
    public static boolean canalMute = false;

    public static boolean analyse(Player player, String msg) {
        if (msg.charAt(0) == '.' && msg.charAt(1) != '.') {
            if (command(msg, "all") && msg.length() > 5) {
                if (player.isInPrison())
                    return true;
                if(canalMute && player.getGroupe() == null) {
                    player.sendMessage("Le canal est indisponible pour une durée indéterminée.");
                    return true;
                }
                if (player.noall) {
                    player.sendMessage(Lang.get(player, 0));
                    return true;
                }
                if (player.getGroupe() == null && System.currentTimeMillis() - player.getGameClient().timeLastTaverne < 10000) {
                    player.sendMessage(Lang.get(player, 2).replace("#1", String.valueOf(10 - ((System.currentTimeMillis() - player.getGameClient().timeLastTaverne) / 1000))));
                    return true;
                }

                player.getGameClient().timeLastTaverne = System.currentTimeMillis();

                String prefix = "<font color='#C35617'>[" + (new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis()))) + "] (" + canal + ") (" + (Config.INSTANCE.getNAME().isEmpty() ? getNameServerById(Config.INSTANCE.getSERVER_ID()) : Config.INSTANCE.getNAME()) + ") <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + player.getName() + "'>" + player.getName() + "</a></b>";

                Logging.globalMessage.info("{}:{}", player.getName(), msg.substring(5, msg.length() - 1));

                final String message = "Im116;" + prefix + "~" + msg.substring(5, msg.length() - 1).replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "</font>";

                World.world.getOnlinePlayers().stream().filter(p -> !p.noall).forEach(p -> p.send(message));
                ExchangeClient.INSTANCE.send("DM" + player.getName() + "|" + getNameServerById(Config.INSTANCE.getSERVER_ID()) + "|" + msg.substring(5, msg.length() - 1).replace("\n", "").replace("\r", "").replace(";", ":").replace("~", "").replace("|", "").replace("<", "").replace(">", "") + "|");
                return true;
            } else if (command(msg, "commandemulti")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 15));
                return true;
            } else if (command(msg, "commandevip")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 16));
                return true;
            } else if (command(msg, "commandebuy")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 17));
                return true;
            } else if (command(msg, "commande")) {
                SocketManager.GAME_SEND_MESSAGE(player, Lang.get(player, 14));
                return true;
            } else if (command(msg, "noall")) {
                if (player.noall) {
                    player.noall = false;
                    player.sendMessage(Lang.get(player, 3));
                } else {
                    player.noall = true;
                    player.sendMessage(Lang.get(player, 4));
                }
                return true;
            } else if (command(msg, "staff")) {
                String message = Lang.get(player, 5);
                boolean vide = true;
                for (Player target : World.world.getOnlinePlayers()) {
                    if (target == null)
                        continue;
                    if (target.getGroupe() == null || target.isInvisible())
                        continue;

                    message += "\n- <b><a href='asfunction:onHref,ShowPlayerPopupMenu," + target.getName() + "'>[" + target.getGroupe().getName() + "] " + target.getName() + "</a></b>";
                    vide = false;
                }
                if (vide)
                    message = Lang.get(player, 6);
                player.sendMessage(message);
                return true;
            } else if (command(msg, "house")) {
                String message = "";
                if (!msg.contains("all")) {
                    message = "L'id de la maison la plus proche est : ";
                    short lstDist = 999;
                    House nearest = null;
                    for (House house : World.world.getHouses().values()) {
                        if (house.getMapId() == player.getCurMap().getId()) {
                            short dist = (short) PathFinding.getDistanceBetween(player.getCurMap(), house.getCellId(), player.getCurCell().getId());
                            if (dist < lstDist) {
                                nearest = house;
                                lstDist = dist;
                            }
                        }
                    }
                    if (nearest != null) message += nearest.getId();
                } else {
                    for (House house : World.world.getHouses().values()) {
                        if (house.getMapId() == player.getCurMap().getId()) {
                            message += "Maison " + house.getId() + " | cellId : " + house.getId();
                        }
                    }
                    if (message.isEmpty()) message = "Aucune maison sur cet carte.";
                }
                player.sendMessage(message);
                return true;
            } else if (command(msg, "parcho")) {
                int prix = 100;
                int points = player.getAccount().getPoints();
                if(player.getisParcho() != 1){
                    if(points < prix) {
                        player.sendMessage("Il vous manque <b>" + (prix - points) + "</b> points boutique pour effectuer cet achat");
                        return true;
                    }
                    else{


                        if (player.getFight() == null) {

                            int val = player.getStats().get(124);
                            int val1 = player.getStats().get(125);
                            int val2 = player.getStats().get(118);
                            int val3 = player.getStats().get(126);
                            int val4 = player.getStats().get(119);
                            int val5 = player.getStats().get(123);

                            if (val + val1 + val2 +val3+val4+val5 != 0){
                                player.sendMessage("Il faut restat vos stats avant de vous parcho");
                                return true;
                            }

                            player.getStats().addOneStat(125, 101);
                            player.getStats().addOneStat(124, 101);
                            player.getStats().addOneStat(118, 101);
                            player.getStats().addOneStat(126, 101);
                            player.getStats().addOneStat(119, 101);
                            player.getStats().addOneStat(123, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN, 101);
                            player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL, 101);
                        }

                        SocketManager.GAME_SEND_STATS_PACKET(player);
                        player.getAccount().setPoints(points - prix);
                        player.sendMessage("Il vous reste <b>" + (points - prix) + "</b> après cet achat");
                        player.setisParcho(1);
                    }
                }
                else{
                    if (player.getFight() == null) {

                        int val = player.getStats().get(124);
                        int val1 = player.getStats().get(125);
                        int val2 = player.getStats().get(118);
                        int val3 = player.getStats().get(126);
                        int val4 = player.getStats().get(119);
                        int val5 = player.getStats().get(123);

                        if (val + val1 + val2 +val3+val4+val5 != 0){
                            player.sendMessage("Il faut restat vos stats avant de vous parcho");
                            return true;
                        }

                        player.getStats().addOneStat(125, 101);
                        player.getStats().addOneStat(124, 101);
                        player.getStats().addOneStat(118, 101);
                        player.getStats().addOneStat(126, 101);
                        player.getStats().addOneStat(119, 101);
                        player.getStats().addOneStat(123, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN, 101);
                        player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL, 101);
                    }

                    SocketManager.GAME_SEND_STATS_PACKET(player);
                    player.sendMessage("Vous êtes de nouveau parchoté");
                }
                return true;
            } else if(command(msg, "spellforget")) {
                player.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL, 0));
                SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+', player);
                return true;
            } else if (command(msg, "hdv")) {
                if(player.getExchangeAction() != null) GameClient.leaveExchange(player);
                if (player.getDeshonor() >= 5) {
                    SocketManager.GAME_SEND_Im_PACKET(player, "183");
                    return true;
                }

                Hdv hdv = World.world.getHdv(-1);
                if (hdv != null) {
                    String info = "1,10,100;" + hdv.getStrCategory() + ";" + hdv.parseTaxe() + ";" + hdv.getLvlMax() + ";" + hdv.getMaxAccountItem() + ";-1;" + hdv.getSellTime();
                    SocketManager.GAME_SEND_ECK_PACKET(player, 11, info);
                    ExchangeAction<Integer> exchangeAction = new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING, -player.getCurMap().getId()); //Rï¿½cupï¿½re l'ID de la map et rend cette valeur nï¿½gative
                    player.setExchangeAction(exchangeAction);
                }
                return true;
            } else if(command(msg, "points")){
                player.sendMessage("Vous avez <b>" + player.getAccount().getPoints() + "</b> points boutique");
                return true;
            }
            else if(command(msg, "ipdrop")){
                if (!player.PlayerList1.isEmpty()) {
                    if(player.ipdrop){
                        player.ipdrop = false;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous ne recuperez plus désormais les drops de vos esclaves");
                    }
                    else{
                        player.ipdrop = true;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous recuperez désormais les drops de vos esclaves");
                    }
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe pour utiliser cette commande");
                }
                return true;
            } else if(command(msg, "noitems")){
                if (!player.PlayerList1.isEmpty() || player.getSlaveLeader()==null) {
                    if(!player.noitems){
                        player.noitems = true;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves ne droperez plus d'items");
                    }
                    else{
                        player.noitems = false;
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous et vos esclaves re-dropez désormais des items");
                    }
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Il faut être maitre d'un groupe ou solo pour utiliser cette commande");
                }
                return true;
            } else if (command(msg, "getmaster")) {
                if(player.getFight() != null){
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                return true;
            }else if (command(msg, "getslave")) {
                if(player.getFight() != null){
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                return true;
            } else if (command(msg, "resetmaitre")) {
                if(player.getFight() != null){
                    return true;
                }
                if(!player.PlayerList1.isEmpty()){
                    player.disposeSlavery();
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Votre liste de suiveur à  été réinitialisée, si vous souhaitez la recréer faites .maitre");
                }
                else {
                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information)</b> Seul un maitre peut relacher ses esclaves");
                }
                return true;
            } else if (command(msg, "restat")) {
                player.getStatsParcho().getMap().clear();
                player.getStats().addOneStat(125,-player.getStats().getEffect(125));
                player.getStats().addOneStat(124,-player.getStats().getEffect(124));
                player.getStats().addOneStat(118,-player.getStats().getEffect(118));
                player.getStats().addOneStat(123,-player.getStats().getEffect(123));
                player.getStats().addOneStat(119,-player.getStats().getEffect(119));
                player.getStats().addOneStat(126,-player.getStats().getEffect(126));
                player.addCapital((player.getLevel() - 1) * 5 - player.get_capital());
                SocketManager.GAME_SEND_STATS_PACKET(player);
                SocketManager.GAME_SEND_Im_PACKET(player,"023;" + (player.getLevel() * 5 - 5));
                return true;
            } else if (command(msg, "deblo")) {
                if (player.cantTP())
                    return true;
                if (player.getFight() != null)
                    return true;
                if (player.getCurCell().isWalkable(true)) {
                    player.sendMessage(Lang.get(player, 7));
                    return true;
                }
                player.teleport(player.getCurMap().getId(), player.getCurMap().getRandomFreeCellId());
                return true;
            } else if(command(msg, "astrub")){
                if (player.isInPrison())
                    return true;
                if (player.cantTP())
                    return true;
                if (player.getFight() != null)
                    return true;

                player.teleport((short) 7411, 311);
                return true;
            }
            else if(command(msg, "save")){
                Database.getStatics().getPlayerData().update(player);
                String	message = "Sauvegarde de l'inventaire terminé";
                SocketManager.GAME_SEND_MESSAGE(player, message);
                return true;
            } if (command(msg, "infos")) {
                long uptime = System.currentTimeMillis()
                        - Config.INSTANCE.getStartTime();
                int jour = (int) (uptime / (1000 * 3600 * 24));
                uptime %= (1000 * 3600 * 24);
                int hour = (int) (uptime / (1000 * 3600));
                uptime %= (1000 * 3600);
                int min = (int) (uptime / (1000 * 60));
                uptime %= (1000 * 60);
                int sec = (int) (uptime / (1000));
                int nbPlayer = GameServer.getClients().size();
                int nbPlayerIp = GameServer.getPlayersNumberByIp();

                String mess = Lang.get(player, 8).replace("#1", String.valueOf(jour)).replace("#2", String.valueOf(hour)).replace("#3", String.valueOf(min)).replace("#4", String.valueOf(sec));
                if (nbPlayer > 0)
                    mess += Lang.get(player, 9).replace("#1", String.valueOf(nbPlayer));
                if (nbPlayerIp > 0)
                    mess += Lang.get(player, 10).replace("#1", String.valueOf(nbPlayerIp));
                player.sendMessage(mess);
                return true;
            } else if(command(msg, "groupe")){
                for (Player z : World.world.getOnlinePlayers()) {
                    if(z != player){

                        if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(z.getAccount().getCurrentIp().toString())){

                            if (z == null || !z.isOnline()) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "n" + z.getName());
                                continue;
                            }
                            if( (z.getParty() != null || player.getParty() != null) && ( z.getParty() == player.getParty())) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans votre groupe");
                                continue;
                            }
                            if (z.getParty() != null) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "a" + z.getName());
                                SocketManager.GAME_SEND_MESSAGE(z,"<b>(Warning)</b> Vous ne pouvez pas rejoindre le groupe de "+ player.getName()+ " car vous avez déjà un groupe");
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> "+ z.getName()+ " ne peut pas vous rejoindre car il est déjà dans un groupe");

                                continue;
                            }
                            if (player.getParty() != null && player.getParty().getPlayers().size() >= 8) {
                                SocketManager.GAME_SEND_GROUP_INVITATION_ERROR(player.getGameClient(), "f");
                                SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning)</b> Vous ne pouvez pas ajouter de joueur supplémentaire à votre groupe");
                                continue;
                            }


                            Party party = player.getParty();
                            if (party == null) {
                                party = new Party(z, player);
                                SocketManager.GAME_SEND_GROUP_CREATE(player.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(player.getGameClient(), party);
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                player.setParty(party);
                                SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(player.getGameClient(), party);
                            }
                            else{
                                SocketManager.GAME_SEND_GROUP_CREATE(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PL_PACKET(z.getGameClient(), party);
                                SocketManager.GAME_SEND_PM_ADD_PACKET_TO_GROUP(party, z);
                                party.addPlayer(z);
                            }
                            z.setParty(party);
                            SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(z.getGameClient(), party);
                            SocketManager.GAME_SEND_PR_PACKET(player);


                        }
                    }
                }
                //player.sendMessage("Le groupe ï¿½ ï¿½tï¿½ crï¿½ï¿½ avec " + player.getAccount().getName() + " comme chef");
                return true;
            }
            else if(command(msg, "boutique")) {
                Boutique.open(player);
                return true;
            }
            else if(command(msg, "tp")){
                if(player.getParty() != null)
                {
                    List<Player> Players = player.getParty().getPlayers();
                    if (player.cantTP()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter votre équipe sur cette carte");
                        return true;
                    }
                    if(GameMap.IsInDj(player.getCurMap())){
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Vous ne pouvez pas téléporter votre équipe sur cette carte");
                        return true;
                    }

                    for(final Player groupPlayer : Players)
                    {
                        if (groupPlayer.getName().equals(player.getName()))
                        {}
                        else if (groupPlayer.getFight() != null) {
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable  pour les personnages en combat");
                            return true;
                        }
                        else{
                            final short mappid = player.getCurMap().getId();
                            final int cellid = player.getCurCell().getId();
                            if (player.getAccount().getCurrentIp().toString().equalsIgnoreCase(groupPlayer.getAccount().getCurrentIp().toString())){
                                //SocketManager.GAME_SEND_MESSAGE(player, message2);
                                if (groupPlayer.cantTP()) {
                                    SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + " ne peut pas etre TP sur la map actuel");
                                    return true;
                                }
                                groupPlayer.teleport(mappid, cellid);
                                SocketManager.GAME_SEND_MESSAGE(player, "<b>(Information) " + groupPlayer.getName() + " </b> a été tp vers vous !");
                            }
                            else {
                                SocketManager.GAME_SEND_MESSAGE(player, "<b>(Warning) " + groupPlayer.getName() + " </b> n'a pas la même IP !");
                            }
                        }
                    }
                }
                else{
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable sans groupe");
                }
                return true;
            }
            else if(command(msg, "banque")) {
                if (player.getAccount().getVip() == 0) {
                    player.sendMessage("Tu n'es pas VIP.");
                    return true;
                }
                final int cost = player.getBankCost();
                if (cost > 0) {
                    final long playerKamas = player.getKamas();
                    final long kamasRemaining = playerKamas - cost;
                    final long bankKamas = player.getAccount().getBankKamas();
                    final long totalKamas = bankKamas + playerKamas;
                    if (kamasRemaining < 0)//Si le joueur n'a pas assez de kamas SUR LUI pour ouvrir la banque
                    {
                        if (bankKamas >= cost) {
                            player.setBankKamas(bankKamas - cost); //On modifie les kamas de la banque
                        } else if (totalKamas >= cost) {
                            player.setKamas(0); //On puise l'entiereter des kamas du joueurs. Ankalike ?
                            player.setBankKamas(totalKamas - cost); //On modifie les kamas de la banque
                            SocketManager.GAME_SEND_STATS_PACKET(player);
                            SocketManager.GAME_SEND_Im_PACKET(player, "020;"
                                    + playerKamas);
                        } else {
                            SocketManager.GAME_SEND_MESSAGE_SERVER(player, "10|"
                                    + cost);
                            return true;
                        }
                    } else
                    //Si le joueur a les kamas sur lui on lui retire directement
                    {
                        player.setKamas(kamasRemaining);
                        SocketManager.GAME_SEND_STATS_PACKET(player);
                        SocketManager.GAME_SEND_Im_PACKET(player, "020;"
                                + cost);
                    }
                }
                SocketManager.GAME_SEND_ECK_PACKET(player.getGameClient(), 5, "");
                SocketManager.GAME_SEND_EL_BANK_PACKET(player);
                player.setAway(true);
                player.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK, 0));
                return true;
            } else if (command(msg, "refreshMobs")) {
                if(player.getAccount().getVip() == 1) {
                    if(player.getCurMap().haveMobFix() || player.getCurMap().getId()==10131
                            || player.getCurMap().getId()==10132 || player.getCurMap().getId()==10133
                            || player.getCurMap().getId()==10134 || player.getCurMap().getId()==10135
                            || player.getCurMap().getId()==10136 || player.getCurMap().getId()==10137
                            || player.getCurMap().getId()==10138){
                        player.sendMessage("Vous ne pouvez pas rafraichir les monstres de cette map.");
                    }
                    else{
                        player.getCurMap().refreshSpawnsWithMaxStars();
                        player.sendMessage("Les monstres sur la map ont été rafraichits.");
                    }
                }
                else {
                    player.sendMessage("Il faut etre VIP pour lancer cette commande.");
                }
                return true;
            } else if (command(msg, "zaap")) {
                if (player.getFight() != null)
                    return true;
                if (player.isInPrison())
                    return true;

                if(player.getAccount().getVip() == 1) {
                    player.openZaapMenu();
                }
                else {
                    player.sendMessage("Il faut etre VIP pour lancer cette commande.");
                }


                //player.getGameClient().removeAction();
                return true;
            } else if (command(msg, "spellboost")) {
                int prix = 50;
                int points = player.getAccount().getPoints();
                if(points < prix) {
                    player.sendMessage("Il vous manque <b>" + (prix - points) + "</b> points boutique pour effectuer cet achat");
                    return true;
                }
                else{
                    player.set_spellPts(player.get_spellPts() + 15);
                    SocketManager.GAME_SEND_STATS_PACKET(player);
                    player.getAccount().setPoints(points - prix);
                    player.sendMessage("Il vous reste <b>" + (points - prix) + "</b> après cet achat");
                }
                return true;
            } else if (command(msg, "vip")) {
                if(player.getAccount().getVip() == 0) {
                    int prix = 400;
                    int points = player.getAccount().getPoints() - prix;
                    if(points < 0) {
                        player.sendMessage("Il vous manque <b>" + (prix - player.getAccount().getPoints()) + "</b> points boutique pour effectuer cet achat");
                        return true;
                    }
                    else{
                        player.getAccount().setVip(1);
                        player.getAccount().setPoints(points);
                        Database.getStatics().getAccountData().update(player.getAccount());
                        player.sendMessage("Vous êtes maintenant VIP ! Il vous reste <b>" + (points - prix) + "</b> après cet achat");
                    }
                }
                else{
                    player.sendMessage("Vous êtes déjà VIP");
                }
                return true;
            } else if (command(msg, "transfert")) {
                if (player.getAccount().getVip() == 0) {
                    player.sendMessage("Tu n'es pas VIP.");
                    return true;
                }
                if (player.isInPrison() || player.getFight() != null )
                    return true;
                if(player.getExchangeAction() == null || player.getExchangeAction().getType() != ExchangeAction.IN_BANK) {
                    player.sendMessage("Tu n'es pas dans ta banque.");
                    return true;
                }

                player.sendTypeMessage("Bank", "Veuillez patienter quelques instants..");
                int count = 0;

                for (GameObject object : new ArrayList<>(player.getItems().values())) {
                    if (object == null || object.getTemplate() == null || !object.getTemplate().getStrTemplate().isEmpty())
                        continue;
                    switch (object.getTemplate().getType()) {
                        case Constant.ITEM_TYPE_OBJET_VIVANT:case Constant.ITEM_TYPE_PRISME:
                        case Constant.ITEM_TYPE_FILET_CAPTURE:case Constant.ITEM_TYPE_CERTIF_MONTURE:
                        case Constant.ITEM_TYPE_OBJET_UTILISABLE:case Constant.ITEM_TYPE_OBJET_ELEVAGE:
                        case Constant.ITEM_TYPE_CADEAUX:case Constant.ITEM_TYPE_PARCHO_RECHERCHE:
                        case Constant.ITEM_TYPE_PIERRE_AME:case Constant.ITEM_TYPE_BOUCLIER:
                        case Constant.ITEM_TYPE_SAC_DOS:case Constant.ITEM_TYPE_OBJET_MISSION:
                        case Constant.ITEM_TYPE_BOISSON:case Constant.ITEM_TYPE_CERTIFICAT_CHANIL:
                        case Constant.ITEM_TYPE_FEE_ARTIFICE:case Constant.ITEM_TYPE_MAITRISE:
                        case Constant.ITEM_TYPE_POTION_SORT:case Constant.ITEM_TYPE_POTION_METIER:
                        case Constant.ITEM_TYPE_POTION_OUBLIE:case Constant.ITEM_TYPE_BONBON:
                        case Constant.ITEM_TYPE_PERSO_SUIVEUR:case Constant.ITEM_TYPE_RP_BUFF:
                        case Constant.ITEM_TYPE_MALEDICTION:case Constant.ITEM_TYPE_BENEDICTION:
                        case Constant.ITEM_TYPE_TRANSFORM:case Constant.ITEM_TYPE_DOCUMENT:
                        case Constant.ITEM_TYPE_QUETES:
                            break;
                        default:
                            count++;
                            player.addInBank(object.getGuid(), object.getQuantity());
                            break;
                    }
                }

                player.sendTypeMessage("Bank", "Le transfert a été effectué, " + count + " objet(s) ont été déplacés.");
                return true;
            }else if (Config.INSTANCE.getTEAM_MATCH() && command(msg, "kolizeum")) {
                if (player.kolizeum != null) {
                    if (player.getParty() != null) {
                        if (player.getParty().isChief(player.getId())) {
                            player.kolizeum.unsubscribe(player.getParty());
                            return true;
                        }
                        player.kolizeum.unsubscribe(player);
                        player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    } else {
                        player.kolizeum.unsubscribe(player);
                        player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                    }
                    return true;
                } else {
                    if (player.getParty() != null) {
                        if (player.getParty().getPlayers().size() < 2) {
                            player.setParty(null);
                            SocketManager.GAME_SEND_PV_PACKET(player.getGameClient(), "");
                            CommandPlayer.analyse(player, ".kolizeum");
                            return true;
                        }
                        if (!player.getParty().isChief(player.getId())) {
                            player.sendMessage("Vous ne pouvez pas inscrire votre groupe, vous n'en êtes pas le chef.");
                            return true;
                        } else if (player.getParty().getPlayers().size() != TeamMatch.PER_TEAM) {
                            player.sendMessage("Pour vous inscrire, vous devez être exactement " + TeamMatch.PER_TEAM
                                    + " joueurs dans votre groupe.");
                            return true;
                        }
                        FightManager.subscribeKolizeum(player, true);
                    } else {
                        FightManager.subscribeKolizeum(player, false);
                    }
                }
                return true;
            } else  if (Config.INSTANCE.getDEATH_MATCH() && command(msg, "deathmatch")) {
                if(player.cantTP()) return true;
                if (player.deathMatch != null) {
                    FightManager.removeDeathMatch(player.deathMatch);
                    player.deathMatch = null;
                    player.sendMessage("Vous venez de vous désincrire de la file d'attente.");
                } else {
                    if(player.getEquippedObjects().size() == 0) {
                        player.sendMessage("Vous devez avoir des objets équipés.");
                    } else {
                        FightManager.subscribeDeathMatch(player);
                    }
                }
                return true;
            } else if(command(msg, "maitre")){
                if (player.getFight() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable en combat");
                    return true;
                }
                if(player.getParty() == null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Commande non-utilisable sans groupe");
                    return true;
                }
                if(player.getSlaveLeader() != null) {
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Erreur)</b> Un esclave ne peut pas devenir maitre");
                    return true;
                }
                for (Player p : player.getParty().getPlayers()) {
                    if (p == null) {
                        continue;
                    }
                    if (!p.getAccount().getCurrentIp().equals(player.getAccount().getCurrentIp())) {
                        continue;
                    }
                    if (p.getCurMap() != player.getCurMap()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> n'est pas sur votre map !");
                        continue;
                    }
                    if (p.getFight() != null) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> est en combat !");
                        continue;
                    }

                    if (p.getId() == player.getId()) {
                        continue;
                    }
                    if (!p.isOnline()) {
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> semble pas joignable !");
                        continue;
                    }
                    if (p.getAccount().getGameClient() == null) {
                        continue;
                    }
                    try {
                        if (p.getSlaveLeader() != null) {
                            p.setSlaveLeader(null) ;
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Warning) " + p.getName() + " </b> avait déjà un maitre :" + p.getSlaveLeader().getName());
                            //continue;
                        }
                        p.setSlaveLeader(player);
                        // Le joueur principal deviens le chef pour les esclaves  !
                        if(player.PlayerList1.contains(p) ) {
                            SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information) " + player.getName() + " </b> est déjà votre maitre !");
                            SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information) " + p.getName() + " </b> est déjà votre esclave !");
                            continue;
                        }
                        else {
                            player.PlayerList1.add(p);
                        }
                        p.teleport(player.getCurMap().getId(), player.getCurCell().getId());
                        //On envoie le message a l'esclave
                        SocketManager.GAME_SEND_MESSAGE(p,"<b>(Information) " + player.getName() + " </b> est désormais votre maitre !");
                        SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information) " + p.getName() + " </b> vous suivras & entreras dans vos combats !");
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }

                return true;
            } else if(command(msg, "pass")){
                if(player.passturn){
                    player.passturn = false;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous ne passerez plus vos tours automatiquement");
                }
                else{
                    player.passturn = true;
                    SocketManager.GAME_SEND_MESSAGE(player,"<b>(Information)</b> Vous passerez vos tours automatiquement");
                }
                return true;
            } else if(command(msg, "event")) {
                if(player.cantTP()) return true;
                return EventManager.getInstance().subscribe(player) == 1;
            } else {
                player.sendMessage(Lang.get(player, 12));
                return true;
            }
        }
        return false;
    }

    private static boolean command(String msg, String command) {
        return msg.length() > command.length() && msg.substring(1, command.length() + 1).equalsIgnoreCase(command);
    }

    private static String getNameServerById(int id) {
        switch (id) {
            case 2 :
            case 1:
                return "Aegnor";
            case 13:
                return "Silouate";
            case 19:
                return "Allister";
            case 22:
                return "Oto Mustam";
            case 37:
                return "Nostalgy";
            case 4001:
                return "Alma";
            case 4002:
                return "Aguabrial";
        }
        return "Unknown";
    }
}