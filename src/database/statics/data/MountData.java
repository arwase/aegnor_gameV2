package database.statics.data;

import client.Player;
import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import entity.mount.Mount;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MountData extends AbstractDAO<Mount> {

    public MountData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
        try (Result result = getData("SELECT * from `world.entity.mounts` WHERE `id` = " + String.valueOf((int) obj) + ";")) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                World.world.addMount(new Mount(RS.getInt("id"), RS.getInt("color"), RS.getInt("sex"), RS.getInt("amour"), RS.getInt("endurance"), RS.getInt("level"), RS.getLong("xp"),
                        RS.getString("name"), RS.getInt("fatigue"), RS.getInt("energy"), RS.getInt("reproductions"), RS.getInt("maturity"), RS.getInt("serenity"), RS.getString("objects"),
                        RS.getString("ancestors"), RS.getString("capacitys"), RS.getInt("size"), RS.getInt("cell"), RS.getShort("map"), RS.getInt("owner"), RS.getInt("orientation"),
                        RS.getLong("fecundatedDate"), RS.getInt("couple"), RS.getInt("savage")));
            }
        } catch (SQLException e) {
            super.sendError("MountData load", e);
        }
    }

    @Override
    public boolean update(Mount mount) {
        String query = "UPDATE `world.entity.mounts` SET `name` = ?, `xp` = ?, `level` = ?, `endurance` = ?, `amour` = ?, `maturity` = ?, `serenity` = ?, `reproductions` = ?, " +
                "`fatigue` = ?, `energy` = ?, `ancestors` = ?, `objects` = ?, `owner` = ?, `capacitys` = ?, `size` = ?, `cell` = ?, `map` = ?, " +
                "`orientation` = ?, `fecundatedDate` = ?, `couple` = ? WHERE `id` = ?;";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setString(1, mount.getName());
            p.getPreparedStatement().setLong(2, mount.getExp());
            p.getPreparedStatement().setInt(3, mount.getLevel());
            p.getPreparedStatement().setInt(4, mount.getEndurance());
            p.getPreparedStatement().setInt(5, mount.getAmour());
            p.getPreparedStatement().setInt(6, mount.getMaturity());
            p.getPreparedStatement().setInt(7, mount.getState());
            p.getPreparedStatement().setInt(8, mount.getReproduction());
            p.getPreparedStatement().setInt(9, mount.getFatigue());
            p.getPreparedStatement().setInt(10, mount.getEnergy());
            p.getPreparedStatement().setString(11, mount.getAncestors());
            p.getPreparedStatement().setString(12, mount.parseObjectsToString());
            p.getPreparedStatement().setInt(13, mount.getOwner());
            p.getPreparedStatement().setString(14, mount.parseCapacitysToString());
            p.getPreparedStatement().setInt(15, mount.getSize());
            p.getPreparedStatement().setInt(16, mount.getCellId());
            p.getPreparedStatement().setInt(17, mount.getMapId());
            p.getPreparedStatement().setInt(18, mount.getOrientation());
            p.getPreparedStatement().setLong(19, mount.getFecundatedDate());
            p.getPreparedStatement().setInt(20, mount.getCouple());
            p.getPreparedStatement().setInt(21, mount.getId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("MountData update", e);
        }
        return false;
    }

    public void delete(int id) {
        String query = "DELETE FROM `world.entity.mounts` WHERE `id` = ?;";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, id);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("MountData delete", e);
        }
    }

    public void delete(Player player) {
        this.delete(player.getMount().getId());
        World.world.delDragoByID(player.getMount().getId());
        player.setMountGiveXp(0);
        player.setMount(null);
        Database.getStatics().getPlayerData().update(player);
    }

    public void add(Mount mount) {
        String query = "INSERT INTO `world.entity.mounts`(`id`, `color`, `sex`, `name`, `xp`, `level`, `endurance`, `amour`, `maturity`, `serenity`, `reproductions`, `fatigue`, `energy`, " +
                "`objects`, `ancestors`, `capacitys`, `size`, `map`, `cell`, `owner`, `orientation`, `fecundatedDate`, `couple`, `savage`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, mount.getId());
            p.getPreparedStatement().setInt(2, mount.getColor());
            p.getPreparedStatement().setInt(3, mount.getSex());
            p.getPreparedStatement().setString(4, mount.getName());
            p.getPreparedStatement().setLong(5, mount.getExp());
            p.getPreparedStatement().setInt(6, mount.getLevel());
            p.getPreparedStatement().setInt(7, mount.getEndurance());
            p.getPreparedStatement().setInt(8, mount.getAmour());
            p.getPreparedStatement().setInt(9, mount.getMaturity());
            p.getPreparedStatement().setInt(10, mount.getState());
            p.getPreparedStatement().setInt(11, mount.getReproduction());
            p.getPreparedStatement().setInt(12, mount.getFatigue());
            p.getPreparedStatement().setInt(13, mount.getEnergy());
            p.getPreparedStatement().setString(14, mount.parseObjectsToString());
            p.getPreparedStatement().setString(15, mount.getAncestors());
            p.getPreparedStatement().setString(16, mount.parseCapacitysToString());
            p.getPreparedStatement().setInt(17, mount.getSize());
            p.getPreparedStatement().setInt(18, mount.getMapId());
            p.getPreparedStatement().setInt(19, mount.getCellId());
            p.getPreparedStatement().setInt(20, mount.getOwner());
            p.getPreparedStatement().setInt(21, mount.getOrientation());
            p.getPreparedStatement().setLong(22, mount.getFecundatedDate());
            p.getPreparedStatement().setInt(23, mount.getCouple());
            p.getPreparedStatement().setInt(24, mount.getSavage());
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("MountData add", e);
        }
    }

    public int getNextId() {
        return Database.getStatics().getWorldEntityData().getNextMountId();
    }
}
