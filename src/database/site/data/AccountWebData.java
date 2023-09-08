package database.site.data;

import ch.qos.logback.classic.Level;
import client.Account;
import client.AccountWeb;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AccountWebData extends AbstractDAO<AccountWeb> {

    public AccountWebData(HikariDataSource source) {
        super(source);
        logger.setLevel(Level.ERROR);
    }

    @Override
    public void load(Object accountid) {
        Result result = null;
        try {
            result = super.getData("SELECT * from users RIGHT JOIN dofus129_in_game_web_account_relations on dofus129_in_game_web_account_relations.azuriom_id = users.id WHERE dofus129_in_game_web_account_relations.dofus_id = " + accountid );
            ResultSet RS = result.resultSet;
            if(RS != null){
                while (RS.next()) {
                    Map<Integer, Integer> accountsID = new HashMap<>();
                    AccountWeb test = World.world.getWebAccount( RS.getInt("id"));
                    if( test == null) {
                        if (RS.getString("email").isEmpty()) continue;

                        accountsID.put(0,RS.getInt("dofus_id"));
                        AccountWeb C = new AccountWeb(RS.getInt("id"), RS.getString("email").toLowerCase(), RS.getString("name"), RS.getInt("is_banned"), RS.getString("last_login_ip"), RS.getString("last_login_at"), RS.getInt("money"),accountsID, RS.getInt("role_id"));
                        World.world.addWebAccount(C);
                        World.world.ReassignAccountWebToAccount(C);
                    }
                    else{
                        test.addAccountId(RS.getInt("dofus_id"));
                    }
                }
            }
            else{
                //System.out.println("Pas de compte web associé à "+ accountid);
            }
        } catch (Exception e) {
            super.sendError("AccountWebData load by account id", e);
        } finally {
            close(result);
        }
    }

    @Override
    public boolean update(AccountWeb obj) {
        return false;
    }

    public void load() {
        Result result = null;

        try {
            result = super.getData("SELECT * from users RIGHT JOIN dofus129_in_game_web_account_relations on dofus129_in_game_web_account_relations.azuriom_id = users.id");
            ResultSet RS = result.resultSet;

            while (RS.next()) {
                Map<Integer, Integer> accountsID = new HashMap<>();
                if(RS.getString("email") == null) continue;

                AccountWeb test = World.world.getWebAccount( RS.getInt("id"));
                if( test == null) {
                    accountsID.put(0, RS.getInt("dofus_id"));
                    AccountWeb a = new AccountWeb(RS.getInt("id"), RS.getString("email").toLowerCase(), RS.getString("name"), RS.getInt("is_banned"), RS.getString("last_login_ip"), RS.getString("last_login_at"), RS.getInt("money"), accountsID, RS.getInt("role_id"));
                    World.world.addWebAccount(a);
                }
                else{
                    test.addAccountId(RS.getInt("dofus_id"));
                }

            }
        } catch (Exception e) {
            super.sendError("AccountWebData load", e);
        } finally {
            close(result);
        }
    }

    public boolean delete(int accountID) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE from dofus129_in_game_web_account_relations WHERE dofus_id = " + accountID);
            p.execute();

            return true;
        } catch (SQLException e) {
            super.sendError("PlayerData delete", e);
        } finally {
            close(p);
        }
        return false;
    }

    /** Points **/
    public int loadPoints(int user) {
        return Database.getSites().getAccountWebData().loadPointsWithUsersDb(user);
    }

    public int loadRole(int user) {
        return Database.getSites().getAccountWebData().loadRoleWithUsersDb(user);
    }

    public void updatePoints(int id, int points) {
        Database.getSites().getAccountWebData().updatePointsWithoutUsersDb(id, points);
    }

    public void updatePointsWithoutUsersDb(int id, int points) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE users SET money = ? WHERE id = ?");
            p.setInt(1, points);
            p.setInt(2, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("AccountWebData updatePoints", e);
        } finally {
            close(p);
        }
    }

    public int loadRoleWithUsersDb(int accountID) {
        Result result = null;
        int role = 1;
        try {
            result = super.getData("SELECT role_id FROM users WHERE id = " + accountID);
            if(result != null) {
                ResultSet RS = result.resultSet;
                if (RS.next()) {
                    role = RS.getInt("role_id");
                }
            }
        } catch (SQLException e) {
            super.sendError("AccountData loadPoints", e);
        } finally {
            close(result);
        }
        return role;
    }

    public int loadPointsWithUsersDb(int accountID) {
        Result result = null;
        int points = 0, user = -1;
        try {
            result = super.getData("SELECT money FROM users WHERE id = " + accountID);
            if(result != null) {
                ResultSet RS = result.resultSet;
                if (RS.next()) {
                    points = RS.getInt("money");
                }
            }
            else{
                points = -1;
            }
        } catch (SQLException e) {
            super.sendError("AccountData loadPoints", e);
        } finally {
            close(result);
        }
        return points;
    }

    /*
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
    public int loadPointsWithoutUsersDb(int user) {
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
    }*/
}