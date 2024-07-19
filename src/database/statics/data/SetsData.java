package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.world.World;
import other.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SetsData extends AbstractDAO<SetsData>  {

    public SetsData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(SetsData obj) {
        return false;
    }

    public void load() {
        try (Result result = getData("SELECT * FROM `sets`;")) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                Sets set = new Sets((RS.getInt("id")), (RS.getInt("playerId")), (RS.getInt("nb")), (RS.getString("name")) , (RS.getString("objects")),(RS.getInt("icon")) );
                World.world.addSets(set);
            }
        } catch (SQLException e) {
            super.sendError("SetsData load", e);
        }
    }

    public int getNextId() {
        int guid = 0;
        try (Result result = getData("SELECT id FROM sets ORDER BY id DESC LIMIT 1")) {
            ResultSet RS = result.getResultSet();
            if (!RS.first())
                guid = 1;
            else
                guid = RS.getInt("id") + 1;
        } catch (SQLException e) {
            super.sendError("SetsData getNextId", e);
        }
        return guid;
    }

    public boolean add(Sets set) {
        String query = "INSERT INTO sets( `id` ,`playerId`, `nb`, `name`, `objects`, `icon`) VALUES (?,?,?,?,?,?)";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, set.getId());
            p.getPreparedStatement().setInt(2, set.getPlayerId());
            p.getPreparedStatement().setInt(3, set.getNb());
            p.getPreparedStatement().setString(4, set.getName());
            p.getPreparedStatement().setString(5, set.getObjects());
            p.getPreparedStatement().setInt(6, set.getIcon());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData add", e);
        }
        return false;
    }

    public boolean delete(Sets set) {
        String query = "DELETE FROM sets WHERE id = ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, set.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData delete", e);
        }
        return false;
    }

    public boolean deletebyPerso(Sets set) {
        String query = "DELETE FROM sets WHERE playerid = ? and nb = ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, set.getPlayerId());
            p.getPreparedStatement().setInt(2, set.getNb());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData deletebyPerso", e);
        }
        return false;
    }

    public boolean updateInfos(Sets set) {
        String query = "UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ? WHERE `id`= ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setString(1, set.getName());
            p.getPreparedStatement().setString(2, set.getObjects());
            p.getPreparedStatement().setInt(3, set.getIcon());
            p.getPreparedStatement().setInt(4, set.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("SetsData updateInfos", e);
        }
        return false;
    }

    public void updateByPerso(Sets set) {
        String query = "UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ? WHERE `nb`= ? and `playerid`= ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setString(1, set.getName());
            p.getPreparedStatement().setString(2, set.getObjects());
            p.getPreparedStatement().setInt(3, set.getIcon());
            p.getPreparedStatement().setInt(4, set.getNb());
            p.getPreparedStatement().setInt(5, set.getPlayerId());
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("SetsData updateByPerso", e);
        }
    }

    public void update(Sets set) {
        String query = "UPDATE `sets` SET `name` = ?, `objects`=?, `icon`= ?, `playerId`= ?, `nb`= ? WHERE `id`= ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setString(1, set.getName());
            p.getPreparedStatement().setString(2, set.getObjects());
            p.getPreparedStatement().setInt(3, set.getIcon());
            p.getPreparedStatement().setInt(4, set.getPlayerId());
            p.getPreparedStatement().setInt(5, set.getNb());
            p.getPreparedStatement().setInt(6, set.getId());
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("SetsData update", e);
        }
    }
}

