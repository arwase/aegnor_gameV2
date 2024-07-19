package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.Database;
import database.statics.AbstractDAO;
import entity.pet.PetEntry;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PetData extends AbstractDAO<PetEntry> {

    public PetData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(PetEntry pets) {
        String query = "UPDATE `world.entity.pets` SET `lastEatDate` = ?, `quantityEat` = ?, `pdv` = ?, `corpulence` = ?, `isEPO` = ? WHERE `id` = ?;";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setLong(1, pets.getLastEatDate());
            p.getPreparedStatement().setInt(2, pets.getQuaEat());
            p.getPreparedStatement().setInt(3, pets.getPdv());
            p.getPreparedStatement().setInt(4, pets.getCorpulence());
            p.getPreparedStatement().setInt(5, (pets.getIsEupeoh() ? 1 : 0));
            p.getPreparedStatement().setInt(6, pets.getObjectId());
            executeUpdate(p);
            return true;
        } catch (SQLException e) {
            super.sendError("PetData update", e);
        }
        return false;
    }

    public int load() {
        String query = "SELECT * FROM `world.entity.pets`;";
        int count = 0;
        try (Result result = getData(query)) {
            ResultSet RS = result.getResultSet();
            while (RS.next()) {
                count++;
                World.world.addPetsEntry(new PetEntry(RS.getInt("id"), RS.getInt("template"), RS.getLong("lastEatDate"), RS.getInt("quantityEat"), RS.getInt("pdv"), RS.getInt("corpulence"), (RS.getInt("isEPO") == 1)));
            }
        } catch (SQLException e) {
            super.sendError("PetData load", e);
        }
        return count;
    }

    public void add(int id, long lastEatDate, int template) {
        String query = "INSERT INTO `world.entity.pets`(`id`, `template`, `lastEatDate`, `quantityEat`, `pdv`, `corpulence`, `isEPO`) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, id);
            p.getPreparedStatement().setInt(2, template);
            p.getPreparedStatement().setLong(3, lastEatDate);
            p.getPreparedStatement().setInt(4, 0);
            p.getPreparedStatement().setInt(5, 10);
            p.getPreparedStatement().setInt(6, 0);
            p.getPreparedStatement().setInt(7, 0);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("PetData add", e);
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM `world.entity.pets` WHERE `id` = ?";
        try (PreparedStatementWrapper p = getPreparedStatement(query)) {
            p.getPreparedStatement().setInt(1, id);
            executeUpdate(p);
        } catch (SQLException e) {
            super.sendError("PetData delete", e);
        }
    }

    public int getNextId() {
        return Database.getStatics().getWorldEntityData().getNextPetId();
    }
}
