package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.world.World;
import other.Sets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SetsData extends AbstractDAO<SetsData>  {

    public SetsData(HikariDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(SetsData obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM `sets`;");
            ResultSet RS = result.resultSet;

            while (RS.next()) {
                Sets set = new Sets((RS.getInt("id")), (RS.getInt("playerId")), (RS.getInt("nb")), (RS.getString("name")) , (RS.getString("objects")),(RS.getInt("icon")) );
                World.world.addSets(set);

            }
        } catch (SQLException e) {
            super.sendError("Subarea_dataData load", e);
        } finally {
            close(result);
        }
    }

    public int getNextId() {
        Result result = null;
        int guid = 0;
        try {
            result = getData("SELECT id FROM sets ORDER BY id DESC LIMIT 1");
            ResultSet RS = result.resultSet;

            if (!RS.first())
                guid = 1;
            else
                guid = RS.getInt("id") + 1;
        } catch (SQLException e) {
            super.sendError("PlayerData getNextId", e);
        } finally {
            close(result);
        }

        return guid;
    }

    public boolean add(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("INSERT INTO sets( `id` ,`playerId`, `nb`, `name`, `objects`, `icon`) VALUES (?,?,?,?,?,?)");
            p.setInt(1, set.getId());
            p.setInt(2, set.getPlayerId());
            p.setInt(3, set.getNb());
            p.setString(4, set.getName());
            p.setString(5, set.getObjects());
            p.setInt(6, set.getIcon());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData add", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean delete(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM sets WHERE id = ?");
            p.setInt(1, set.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData delete", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean deletebyPerso(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("DELETE FROM sets WHERE playerid = ? and nb = ?");
            p.setInt(1, set.getPlayerId());
            p.setInt(2, set.getNb());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData deletebyPerso", e);
        } finally {
            close(p);
        }
        return false;
    }

    public boolean updateInfos(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ? WHERE `id`= ?");
            p.setString(1, set.getName());
            p.setString(2, set.getObjects());
            p.setInt(3, set.getIcon());
            p.setInt(4, set.getId());
            execute(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData updateInfos", e);
        } finally {
            close(p);
        }
        return false;
    }

    public void updateByPerso(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ? WHERE `nb`= ? and `playerid`= ?");
            p.setString(1, set.getName());
            p.setString(2, set.getObjects());
            p.setInt(3, set.getIcon());
            p.setInt(4, set.getNb());
            p.setInt(5, set.getPlayerId());
            execute(p);
        } catch (SQLException e) {
            super.sendError("SetsData updateByPerso", e);
        } finally {
            close(p);
        }
    }

    public void update(Sets set) {
        PreparedStatement p = null;
        try {
            p = getPreparedStatement("UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ?, `playerId`= ?, `nb`= ? WHERE `id`= ?");
            p.setString(1, set.getName());
            p.setString(2, set.getObjects());
            p.setInt(3, set.getIcon());
            p.setInt(4, set.getPlayerId());
            p.setInt(5, set.getNb());
            p.setInt(6, set.getId());
            execute(p);
        } catch (SQLException e) {
            super.sendError("SetsData update", e);
        } finally {
            close(p);
        }
    }

}

