package database.dynamics.data;

import area.map.GameMap;
import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import entity.monster.Monster;
import game.world.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DropData extends AbstractDAO<World.Drop> {
    public DropData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(World.Drop obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * from drops");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                Monster MT = World.world.getMonstre(RS.getInt("monsterId"));
                if (World.world.getObjTemplate(RS.getInt("objectId")) != null && MT != null) {
                    String action = RS.getString("action");
                    String condition = "";

                    if (!action.equals("-1") && !action.equals("1")
                            && action.contains(":")) {
                        condition = action.split(":")[1];
                        action = action.split(":")[0];
                    }
                    ArrayList<Double> percents = new ArrayList<>();
                    percents.add(RS.getDouble("percentGrade1"));
                    percents.add(RS.getDouble("percentGrade2"));
                    percents.add(RS.getDouble("percentGrade3"));
                    percents.add(RS.getDouble("percentGrade4"));
                    percents.add(RS.getDouble("percentGrade5"));

                    MT.addDrop(new World.Drop(RS.getInt("objectId"), percents, RS.getInt("ceil"), Integer.parseInt(action), RS.getInt("level"), condition));
                } else {
                    if(MT == null && RS.getInt("monsterId") == 0) {
                        String action = RS.getString("action");
                        String condition = "";

                        if (!action.equals("-1") && !action.equals("1")
                                && action.contains(":")) {
                            condition = action.split(":")[1];
                            action = action.split(":")[0];
                        }
                        ArrayList<Double> percents = new ArrayList<>();
                        percents.add(RS.getDouble("percentGrade1"));
                        percents.add(RS.getDouble("percentGrade2"));
                        percents.add(RS.getDouble("percentGrade3"));
                        percents.add(RS.getDouble("percentGrade4"));
                        percents.add(RS.getDouble("percentGrade5"));
                        World.Drop drop = new World.Drop(RS.getInt("objectId"), percents, RS.getInt("ceil"), Integer.parseInt(action), RS.getInt("level"), condition);
                        World.world.getMonstres().stream().filter(monster -> monster != null).forEach(monster -> monster.addDrop(drop));
                    }
                }
            }
        } catch (SQLException e) {
            super.sendError("DropData load", e);
        } finally {
            close(result);
        }
    }
    public boolean insertDrop(int monsterID, int objID, int pp, double taux, int action, String objName)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE `drops` WHERE monsterId = ? AND objectId = ?");
            p.setInt(1, monsterID);
            p.setInt(2, objID);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("DropData insertDrop DELETE", e);
        } finally {
            close(p);
        }
        PreparedStatement p2 = null;
        try {
            p2 = getPreparedStatement("INSERT INTO `drops` (`monsterId`, `objectName`, `objectId`, `percentGrade1`, `percentGrade2`, `percentGrade3`, `percentGrade4`, `percentGrade5`, `ceil`, `action`) VALUES (?,?,?,?,?,?,?,?,?,?);");
            p2.setInt(1, monsterID);
            p2.setString(2, objName);
            p2.setInt(3, objID);
            p2.setDouble(4, taux);
            p2.setDouble(5, taux);
            p2.setDouble(6, taux);
            p2.setDouble(7, taux);
            p2.setDouble(8, taux);
            p2.setInt(3, pp);
            p2.setInt(3, action);
            execute(p2);
            return true;
        } catch (SQLException e) {
            super.sendError("DropData insertDrop INSERT", e);
        } finally {
            close(p2);
        }
        return false;
    }
    public boolean deleteDrop(int objID, int monsterID)
    {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE `drops` WHERE monsterId = ? AND objectId = ?");
            p.setInt(1, monsterID);
            p.setInt(2, objID);
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("DropData deleteDrop", e);
        } finally {
            close(p);
        }
        return false;
    }

    public void reload() {
        World.world.getMonstres().stream().filter(m -> m != null).filter(m -> m.getDrops() != null).forEach(m -> m.getDrops().clear());
        load();
    }
}
