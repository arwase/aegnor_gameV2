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
        Result result = null;
        try {
            result = getData("SELECT * FROM `world.entity.objects`;");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                int id = RS.getInt("id");
                int template = RS.getInt("template");
                int quantity = RS.getInt("quantity");
                int position = RS.getInt("position");
                String stats = RS.getString("stats");
                int puit = RS.getInt("puit");
                int rarity = RS.getInt("rarity");
                int mimibiote = RS.getInt("mimibiote");
                if(quantity == 0) continue;
                World.world.addGameObject(World.world.newObjet(id, template, quantity, position, stats, puit,rarity,mimibiote), false);
            }
        } catch (SQLException e) {
            super.sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
    }

    @Override
    public void load(Object obj) {
        Result result = null;
        try {
            result = getData("SELECT * FROM `world.entity.objects` WHERE `id` IN (" + obj + ");");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                int id = RS.getInt("id");
                int template = RS.getInt("template");
                int quantity = RS.getInt("quantity");
                int position = RS.getInt("position");
                String stats = RS.getString("stats");
                int puit = RS.getInt("puit");
                int rarity = RS.getInt("rarity");
                int mimibiote = RS.getInt("mimibiote");
                if(quantity == 0) continue;
                World.world.addGameObject(World.world.newObjet(id, template, quantity, position, stats, puit, rarity, mimibiote), false);
            }
        } catch (SQLException e) {
            super.sendError("ObjectData load", e);
            Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
    }

    @Override
    public boolean update(GameObject object) {
        if (object == null)
            return false;
        if (object.getTemplate() == null)
            return false;

        PreparedStatement p = null;
        try {

            p = getPreparedStatement("UPDATE `world.entity.objects` SET `template` = ?, `quantity` = ?, `position` = ?, `puit` = ?, `rarity` = ?, `mimibiote` = ?, `stats` = ? WHERE `id` = ?;");
            p.setInt(1, object.getTemplate().getId());
            p.setInt(2, object.getQuantity());
            p.setInt(3, object.getPosition());
            p.setInt(4, object.getPuit());
            p.setInt(5, object.getRarity());
            p.setInt(6, object.getMimibiote());
            p.setString(7, object.parseToSave());
            p.setInt(8, object.getGuid());


            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("ObjectData update", e);
        } finally {
            close(p);
        }
        return false;
    }

    public void insert(GameObject object) {
        if (object == null) {
            super.sendError("ObjectData insert", new Exception("Object null"));
            return;
        } else if (object.getTemplate() == null) {
            super.sendError("ObjectData insert", new Exception("Template null"));
            return;
        }
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("REPLACE INTO `world.entity.objects` VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
            p.setInt(1, object.getGuid());
            p.setInt(2, object.getTemplate().getId());
            p.setInt(3, object.getQuantity());
            p.setInt(4, object.getPosition());
            p.setString(5, object.parseToSave());
            p.setInt(6, object.getPuit());
            p.setInt(7, object.getRarity());
            p.setInt(8, object.getMimibiote());
            execute(p);
        } catch (SQLException e) {
            super.sendError("ObjectData insert", e);
        } finally {
            close(p);
        }
    }

    public void delete(int id) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM `world.entity.objects` WHERE id = ?;");
            p.setInt(1, id);
            execute(p);
        } catch (SQLException e) {
            super.sendError("ObjectData delete", e);
        } finally {
            close(p);
        }
    }

    // TODO : changé cette gestion de merde des IDs qui ecrase certains items
    public int getNextId() {
        return Database.getStatics().getWorldEntityData().getNextObjectId();
        //  final DataType<Integer> queue = new DataType<>((byte) 2);
        //  public int getNextId() {

            /*final DataQueue.Queue<Integer> queue = new DataQueue.Queue<>((byte) 2);
            try {
                synchronized (queue) {
                    long count = DataQueue.count();
                    DataQueue.queues.put(count, queue);
                    Main.exchangeClient.send("DI" + queue.getType() + count);
                    queue.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return queue.getValue();
            //  }*/

       // return queue.getValue();
    }
}
