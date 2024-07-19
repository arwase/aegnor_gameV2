package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import object.GameObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObvejivanData extends AbstractDAO<GameObject> {

    public ObvejivanData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(GameObject obj) {
        return false;
    }

    public void add(GameObject obvijevan, GameObject object) {
        String query = "INSERT INTO `world.entity.obvijevans`(`id`, `template`) VALUES(?, ?);";
        try (PreparedStatementWrapper stmt = getPreparedStatement(query)) {
            PreparedStatement p = stmt.getPreparedStatement();
            p.setInt(1, object.getGuid());
            p.setInt(2, obvijevan.getTemplate().getId());
            p.executeUpdate();
        } catch (Exception e) {
            sendError("ObvejivanData add", e);
        }
    }

    public int getAndDelete(GameObject object, boolean delete) {
        String selectQuery = "SELECT * FROM `world.entity.obvijevans` WHERE `id` = ?;";
        String deleteQuery = "DELETE FROM `world.entity.obvijevans` WHERE id = ?;";
        int template = -1;

        try (PreparedStatementWrapper selectStmt = getPreparedStatement(selectQuery)) {
            PreparedStatement selectPs = selectStmt.getPreparedStatement();
            selectPs.setInt(1, object.getGuid());
            try (ResultSet resultSet = selectPs.executeQuery()) {
                if (resultSet.next()) {
                    template = resultSet.getInt("template");
                    if (delete) {
                        try (PreparedStatementWrapper deleteStmt = getPreparedStatement(deleteQuery)) {
                            PreparedStatement deletePs = deleteStmt.getPreparedStatement();
                            deletePs.setInt(1, object.getGuid());
                            deletePs.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            sendError("ObvejivanData getAndDelete", e);
        }
        return template;
    }
}
