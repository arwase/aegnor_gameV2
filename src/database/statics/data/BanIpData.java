package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BanIpData extends AbstractDAO<Object> {
    public BanIpData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Object obj) {
        return false;
    }

    public boolean add(String ip) {
        String query = "INSERT INTO `banip` VALUES (?)";
        try (PreparedStatementWrapper ps = getPreparedStatement(query)) {
            ps.getPreparedStatement().setString(1, ip);
            executeUpdate(ps);
            return true;
        } catch (SQLException e) {
            super.sendError("BanipData add", e);
        }
        return false;
    }

    public boolean delete(String ip) {
        String query = "DELETE FROM `banip` WHERE `ip` = ?";
        try (PreparedStatementWrapper ps = getPreparedStatement(query)) {
            ps.getPreparedStatement().setString(1, ip);
            executeUpdate(ps);
            return true;
        } catch (SQLException e) {
            super.sendError("BanipData delete", e);
        }
        return false;
    }
}
