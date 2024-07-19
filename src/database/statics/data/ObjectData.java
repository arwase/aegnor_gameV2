package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import game.world.World;
import kernel.Main;
import object.GameObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectData extends AbstractDAO<GameObject> {

    public ObjectData(HikariDataSource dataSource) {
        super(dataSource);
    }

    public void load() {
        String query = "SELECT * FROM `world.entity.objects`;";
        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    int id = RS.getInt("id");
                    int template = RS.getInt("template");
                    int quantity = RS.getInt("quantity");
                    int position = RS.getInt("position");
                    String stats = RS.getString("stats");
                    int puit = RS.getInt("puit");
                    int rarity = RS.getInt("rarity");
                    int mimibiote = RS.getInt("mimibiote");
                    if (quantity == 0) continue;
                    World.world.addGameObject(World.world.newObjet(id, template, quantity, position, stats, puit, rarity, mimibiote), false);
                }
            }
        } catch (SQLException e) {
            sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `world.entity.objects` WHERE `id` IN (" + obj + ");";
        try (Result result = getData(query)) {
            if (result != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    int id = RS.getInt("id");
                    int template = RS.getInt("template");
                    int quantity = RS.getInt("quantity");
                    int position = RS.getInt("position");
                    String stats = RS.getString("stats");
                    int puit = RS.getInt("puit");
                    int rarity = RS.getInt("rarity");
                    int mimibiote = RS.getInt("mimibiote");
                    if (quantity == 0) continue;
                    World.world.addGameObject(World.world.newObjet(id, template, quantity, position, stats, puit, rarity, mimibiote), false);
                }
            }
        } catch (SQLException e) {
            sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        }
    }

    @Override
    public boolean update(GameObject object) {
        if (object == null || object.getTemplate() == null)
            return false;

        String query = "UPDATE `world.entity.objects` SET `template` = ?, `quantity` = ?, `position` = ?, `puit` = ?, `rarity` = ?, `mimibiote` = ?, `stats` = ? WHERE `id` = ?;";
        try (PreparedStatementWrapper stmt = getPreparedStatement(query)) {
            PreparedStatement p = stmt.getPreparedStatement();
            p.setInt(1, object.getTemplate().getId());
            p.setInt(2, object.getQuantity());
            p.setInt(3, object.getPosition());
            p.setInt(4, object.getPuit());
            p.setInt(5, object.getRarity());
            p.setInt(6, object.getMimibiote());
            p.setString(7, object.parseToSave());
            p.setInt(8, object.getGuid());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            sendError("ObjectData update", e);
        }
        return false;
    }

    public void insert(GameObject object) {
        if (object == null || object.getTemplate() == null) {
            sendError("ObjectData insert", new Exception("Object or template is null"));
            return;
        }

        String query = "REPLACE INTO `world.entity.objects` VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatementWrapper stmt = getPreparedStatement(query)) {
            PreparedStatement p = stmt.getPreparedStatement();
            p.setInt(1, object.getGuid());
            p.setInt(2, object.getTemplate().getId());
            p.setInt(3, object.getQuantity());
            p.setInt(4, object.getPosition());
            p.setString(5, object.parseToSave());
            p.setInt(6, object.getPuit());
            p.setInt(7, object.getRarity());
            p.setInt(8, object.getMimibiote());
            p.executeUpdate();
        } catch (SQLException e) {
            sendError("ObjectData insert", e);
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM `world.entity.objects` WHERE id = ?;";
        try (PreparedStatementWrapper stmt = getPreparedStatement(query)) {
            PreparedStatement p = stmt.getPreparedStatement();
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            sendError("ObjectData delete", e);
        }
    }

    // TODO : changer cette gestion de merde des IDs qui écrase certains items
    public int getNextId() {
        return Database.getStatics().getWorldEntityData().getNextObjectId();
    }
}
