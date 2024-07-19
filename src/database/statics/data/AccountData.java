package database.statics.data;

import ch.qos.logback.classic.Level;
import client.Account;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountData extends AbstractDAO<Account> {

    public AccountData(HikariDataSource source) {
        super(source);
        logger.setLevel(Level.ERROR);
    }

    public void load(Object id) {
        String query = "SELECT * FROM accounts WHERE guid = " + id.toString();
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (World.world.getAccount(RS.getInt("guid")) != null) continue;

                    Account account = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"),
                            RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"),
                            RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"),
                            RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"),
                            RS.getInt("points"), RS.getString("dateRegister"));
                    World.world.addAccount(account);
                    World.world.ReassignAccountToChar(account);
                    Database.getSites().getAccountWebData().loadWebAccountFromGameAccount(account);
                }
            }
        } catch (Exception e) {
            super.sendError("AccountData load id", e);
        }
    }

    public void load() {
        String query = "SELECT * from accounts";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    if (RS.getString("pseudo").isEmpty()) continue;
                    Account account = new Account(RS.getInt("guid"), RS.getString("account").toLowerCase(), RS.getString("pseudo"),
                            RS.getString("reponse"), (RS.getInt("banned") == 1), RS.getString("lastIP"), RS.getString("lastConnectionDate"),
                            RS.getString("friends"), RS.getString("enemy"), RS.getLong("subscribe"), RS.getLong("muteTime"),
                            RS.getString("mutePseudo"), RS.getString("lastVoteIP"), RS.getString("heurevote"), RS.getInt("vip"),
                            RS.getInt("points"), RS.getString("dateRegister"));
                    World.world.addAccount(account);
                }
            }
        } catch (Exception e) {
            super.sendError("AccountData load", e);
        }
    }

    public long getSubscribe(int id) {
        long subscribe = 0;
        String query = "SELECT guid, subscribe FROM accounts WHERE guid = " + id;
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    subscribe = RS.getLong("subscribe");
                }
            }
        } catch (Exception e) {
            super.sendError("AccountData getSubscribe", e);
        }
        return subscribe;
    }

    public void updateVoteAll() {
        String query = "SELECT guid, heurevote, lastVoteIP from accounts";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    Account account = World.world.getAccount(RS.getInt("guid"));
                    if (account == null) continue;
                    account.updateVote(RS.getString("heurevote"), RS.getString("lastVoteIP"));
                }
            }
        } catch (SQLException e) {
            super.sendError("AccountData updateVoteAll", e);
        }
    }

    @Override
    public boolean update(Account acc) {
        String query = "UPDATE accounts SET banned = ?, friends = ?, enemy = ?, muteTime = ?, mutePseudo = ?, vip = ? WHERE guid = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, acc.isBanned() ? 1 : 0);
            p.setString(2, acc.parseFriendListToDB());
            p.setString(3, acc.parseEnemyListToDB());
            p.setLong(4, acc.getMuteTime());
            p.setString(5, acc.getMutePseudo());
            p.setInt(6, acc.getVip());
            p.setInt(7, acc.getId());
            p.executeUpdate();
            return true;
        } catch (Exception e) {
            super.sendError("AccountData update", e);
        }
        return false;
    }

    public void updateVip(Account account) {
        String query = "UPDATE accounts SET `vip` = ? WHERE `guid` = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, account.getVip());
            p.setInt(2, account.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            super.sendError("AccountData updateVip", e);
        }
    }

    public void updateLastConnection(Account account) {
        String query = "UPDATE accounts SET `lastIP` = ?, `lastConnectionDate` = ? WHERE `guid` = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setString(1, account.getCurrentIp());
            p.setString(2, account.getLastConnectionDate());
            p.setInt(3, account.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            super.sendError("AccountData updateLastConnection", e);
        }
    }

    public void setLogged(int id, int logged) {
        String query = "UPDATE `accounts` SET `logged` = ? WHERE `guid` = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, logged);
            p.setInt(2, id);
            p.executeUpdate();
        } catch (SQLException e) {
            super.sendError("AccountData setLogged", e);
        }
    }

    public boolean updateBannedTime(Account acc, long time) {
        String query = "UPDATE accounts SET banned = ?, bannedTime = ? WHERE guid = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, acc.isBanned() ? 1 : 0);
            p.setLong(2, time);
            p.setInt(3, acc.getId());
            p.executeUpdate();
            return true;
        } catch (Exception e) {
            super.sendError("AccountData updateBannedTime", e);
        }
        return false;
    }

    public boolean delete(Account acc) {
        String query = "DELETE FROM accounts WHERE guid = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, acc.getId());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            super.sendError("AccountData delete", e);
        }
        return false;
    }

    /** Points **/
    public int loadPoints(String user) {
        return Database.getStatics().getAccountData().loadPointsWithoutUsersDb(user);
    }

    public void updatePoints(int id, int points) {
        Database.getStatics().getAccountData().updatePointsWithoutUsersDb(id, points);
    }

    public int loadPointsWithoutUsersDb(String user) {
        int points = 0;
        String query = "SELECT * from accounts WHERE `account` LIKE '" + user + "'";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                if (RS.next()) {
                    points = RS.getInt("points");
                }
            }
        } catch (SQLException e) {
            super.sendError("AccountData loadPointsWithoutUsersDb", e);
        }
        return points;
    }

    public void updatePointsWithoutUsersDb(int id, int points) {
        String query = "UPDATE accounts SET `points` = ? WHERE `guid` = ?";
        try (PreparedStatementWrapper statement = getPreparedStatement(query)) {
            PreparedStatement p = statement.getPreparedStatement();
            p.setInt(1, points);
            p.setInt(2, id);
            p.executeUpdate();
        } catch (SQLException e) {
            super.sendError("AccountData updatePointsWithoutUsersDb", e);
        }
    }

}