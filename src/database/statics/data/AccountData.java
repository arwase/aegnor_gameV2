package database.statics.data;

import ch.qos.logback.classic.Level;
import client.AccountWeb;
import client.Player;
import com.zaxxer.hikari.HikariDataSource;
import client.Account;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;
import kernel.Config;
import kernel.Constant;
import kernel.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class AccountData extends AbstractDAO<Account> {

    public AccountData(HikariDataSource source) {
        super(source);
        logger.setLevel(Level.ERROR);
    }

    public void load(Object id) {
        Result result = null;
        try {
            result = super.getData("SELECT * FROM accounts WHERE guid = " + id.toString());
            ResultSet RS = result.resultSet;

            while (RS.next()) {
                Account a = World.world.getAccount(RS.getInt("guid"));
                if (a != null && a.isOnline())
                    continue;
                Account C = null;
                AccountWeb aw = World.world.getWebAccountBygameAccountid(RS.getInt("guid"));
                if(aw == null) {
                    C = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"), RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"), RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"), RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"),RS.getInt("points"));
                }
                else {
                    C = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"), RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"), RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"), RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"), aw.getId(),RS.getInt("points"));
                }
                World.world.addAccount(C);
                World.world.ReassignAccountToChar(C);
            }
        } catch (Exception e) {
            super.sendError("AccountData load id", e);
        } finally {
            close(result);
        }
    }

    public void load() {
        Result result = null;
        try {
            result = super.getData("SELECT * from accounts");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                AccountWeb aw = null;
                aw = World.world.getWebAccountBygameAccountid(RS.getInt("guid"));
                if(RS.getString("pseudo").isEmpty()) continue;
                Account a = null;
                if(aw == null){
                    a = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"), RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"), RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"), RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"),RS.getInt("points"));
                }
                else {
                    a = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"), RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"), RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"), RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"), aw.getId(),RS.getInt("points"));
                }
                World.world.addAccount(a);
            }
        } catch (Exception e) {
            super.sendError("AccountData load", e);
        } finally {
            close(result);
        }
    }

    public long getSubscribe(int id) {
        long subscribe = 0;
        Result result = null;
        try {
            result = super.getData("SELECT guid, subscribe FROM accounts WHERE guid = " + id);

            if(result != null) {
                ResultSet RS = result.resultSet;
                while (RS.next()) {
                    subscribe = RS.getLong("subscribe");
                }
            }
        } catch (Exception e) {
            super.sendError("AccountData load id", e);
        } finally {
            close(result);
        }
        return subscribe;
    }

    public void updateVoteAll() {
        Result result = null;
        Account a = null;
        try {
            result = super.getData("SELECT guid, heurevote, lastVoteIP from accounts");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                a = World.world.getAccount(RS.getInt("guid"));
                if (a == null)
                    continue;
                a.updateVote(RS.getString("heurevote"), RS.getString("lastVoteIP"));
            }
        } catch (SQLException e) {
            super.sendError("AccountData updateVoteAll", e);
        } finally {
            close(result);
        }
    }

    @Override
    public boolean update(Account acc) {
        PreparedStatement statement = null;
        try {
            statement = getPreparedStatement("UPDATE accounts SET banned = '"
                    + (acc.isBanned() ? 1 : 0) + "', friends = '"
                    + acc.parseFriendListToDB() + "', enemy = '"
                    + acc.parseEnemyListToDB() + "', muteTime = '"
                    + acc.getMuteTime() + "', mutePseudo = '"
                    + acc.getMutePseudo() + "', vip = '"+ acc.getVip() +"' WHERE guid = '" + acc.getId()
                    + "'");
            execute(statement);
            return true;
        } catch (Exception e) {
            super.sendError("AccountData update", e);
        } finally {
            close(statement);
        }
        return false;
    }

    public void updateVip(Account compte)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE accounts SET `vip` = ? WHERE `guid` = ?");
            p.setInt(1, compte.getVip());
            p.setInt(2, compte.getId());
            execute(p);
        } catch (SQLException e) {
            super.sendError("AccountData updateLastConnection", e);
        } finally {
            close(p);
        }
    }

    public void updateLastConnection(Account compte) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE accounts SET `lastIP` = ?, `lastConnectionDate` = ? WHERE `guid` = ?");
            p.setString(1, compte.getCurrentIp());
            p.setString(2, compte.getLastConnectionDate());
            p.setInt(3, compte.getId());
            execute(p);
        } catch (SQLException e) {
            super.sendError("AccountData updateLastConnection", e);
        } finally {
            close(p);
        }
    }

    public void setLogged(int id, int logged) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `accounts` SET `logged` = ? WHERE `guid` = ?;");
            p.setInt(1, logged);
            p.setInt(2, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("AccountData setLogged", e);
        } finally {
            close(p);
        }
    }

    public boolean updateBannedTime(Account acc, long time) {
        PreparedStatement statement = null;
        try {
            statement = getPreparedStatement("UPDATE accounts SET banned = '"
                    + (acc.isBanned() ? 1 : 0) + "', bannedTime = '"
                    + time + "' WHERE guid = '" + acc.getId()
                    + "'");
            execute(statement);
            return true;
        } catch (Exception e) {
            super.sendError("AccountData update", e);
        } finally {
            close(statement);
        }
        return false;
    }

    public boolean delete(Account acc) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM accounts WHERE id = ?");
            p.setInt(1, acc.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("PlayerData delete", e);
        } finally {
            close(p);
        }
        return false;
    }

    /** Points **/
    /** Points **/
    public int loadPoints(String user) {
        return Database.getStatics().getAccountData().loadPointsWithoutUsersDb(user);
    }

    public void updatePoints(int id, int points) {
        Database.getStatics().getAccountData().updatePointsWithoutUsersDb(id, points);
    }

    public int loadPointsWithoutUsersDb(String user) {
        Result result = null;
        int points = 0;
        try {
            result = super.getData("SELECT * from accounts WHERE `account` LIKE '"
                    + user + "'");
            ResultSet RS = result.resultSet;
            if (RS.next()) {
                points = RS.getInt("points");
            }
        } catch (SQLException e) {
            super.sendError("AccountData loadPoints", e);
        } finally {
            close(result);
        }
        return points;
    }

    public void updatePointsWithoutUsersDb(int id, int points) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE accounts SET `points` = ? WHERE `guid` = ?");
            p.setInt(1, points);
            p.setInt(2, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("AccountData updatePoints", e);
        } finally {
            close(p);
        }
    }

    public int loadPointsWithUsersDb(String account) {
        Result result = null;
        int points = 0, user = -1;
        try {
            result = super.getData("SELECT account, users FROM `accounts` WHERE `account` LIKE '" + account + "'");
            ResultSet RS = result.resultSet;
            if (RS.next()) user = RS.getInt("users");
            close(result);

            if(user == -1) {
                result = super.getData("SELECT id, points FROM `users` WHERE `id` = " + user + ";");
                RS = result.resultSet;
                if (RS.next()) points = RS.getInt("users");
            }
        } catch (SQLException e) {
            super.sendError("AccountData loadPoints", e);
        } finally {
            close(result);
        }
        return points;
    }

    public void updatePointsWithUsersDb(int id, int points) {
        PreparedStatement p = null;
        int user = -1;
        try {
            Result result = super.getData("SELECT guid, users FROM `accounts` WHERE `guid` LIKE '" + id + "'");
            ResultSet RS = result.resultSet;
            if (RS.next()) user = RS.getInt("users");
            close(result);

            if(user != -1) {
                p = getPreparedStatement("UPDATE `users` SET `points` = ? WHERE `id` = ?;");
                p.setInt(1, points);
                p.setInt(2, id);
                execute(p);
            }
        } catch (SQLException e) {
            super.sendError("AccountData updatePoints", e);
        } finally {
            close(p);
        }
    }

    public void loadByAccountWebId(int id) {

        Result result = null;
        try {
            result = getData("SELECT * FROM players WHERE account = '" + id + "'");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                if (RS.getInt("server") != Config.INSTANCE.getSERVER_ID())
                    continue;

                Player p = World.world.getPlayer(RS.getInt("id"));
                if (p != null) {
                    if (p.getFight() != null) {
                        continue;
                    }
                }

                HashMap<Integer, Integer> stats = new HashMap<Integer, Integer>();

                stats.put(Constant.STATS_ADD_VITA, RS.getInt("vitalite"));
                stats.put(Constant.STATS_ADD_FORC, RS.getInt("force"));
                stats.put(Constant.STATS_ADD_SAGE, RS.getInt("sagesse"));
                stats.put(Constant.STATS_ADD_INTE, RS.getInt("intelligence"));
                stats.put(Constant.STATS_ADD_CHAN, RS.getInt("chance"));
                stats.put(Constant.STATS_ADD_AGIL, RS.getInt("agilite"));
                Player player = new Player(RS.getInt("id"), RS.getString("name"), RS.getInt("groupe"), RS.getInt("sexe"), RS.getInt("class"), RS.getInt("color1"), RS.getInt("color2"), RS.getInt("color3"), RS.getLong("kamas"), RS.getInt("spellboost"), RS.getInt("capital"), RS.getInt("energy"), RS.getInt("level"), RS.getLong("xp"), RS.getInt("size"), RS.getInt("gfx"), RS.getByte("alignement"), RS.getInt("account"), stats, RS.getByte("seeFriend"), RS.getByte("seeAlign"), RS.getByte("seeSeller"), RS.getString("canaux"), RS.getShort("map"), RS.getInt("cell"), RS.getString("objets"), RS.getString("storeObjets"), RS.getInt("pdvper"), RS.getString("spells"), RS.getString("savepos"), RS.getString("jobs"), RS.getInt("mountxpgive"), RS.getInt("mount"), RS.getInt("honor"), RS.getInt("deshonor"), RS.getInt("alvl"), RS.getString("zaaps"), RS.getByte("title"), RS.getInt("wife"), RS.getString("morphMode"), RS.getString("allTitle"), RS.getString("emotes"), RS.getLong("prison"), false, RS.getString("parcho"), RS.getLong("timeDeblo"), RS.getBoolean("noall"), RS.getString("deadInformation"), RS.getByte("needRestat"), RS.getLong("totalKills"), RS.getInt("isParcho"));

                if(p != null)
                    player.setNeededEndFight(p.needEndFight(), p.hasMobGroup());
                player.VerifAndChangeItemPlace();
                World.world.addPlayer(player);
                int guild = Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));
                if (guild >= 0)
                    player.setGuildMember(World.world.getGuild(guild).getMember(RS.getInt("id")));
            }
        } catch (SQLException e) {
            super.sendError("PlayerData loadByAccountId", e);
            Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
    }

}