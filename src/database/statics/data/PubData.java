package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.scheduler.entity.WorldPub;
import kernel.Config;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PubData extends AbstractDAO<Object> {

    public PubData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

    @Override
    public void load(Object obj) {
        String query = "SELECT * FROM `pubs` WHERE `server` = " + Config.INSTANCE.getSERVER_ID() + ";";
        try (Result result = getData(query)) {
            if (result != null && result.getResultSet() != null) {
                ResultSet RS = result.getResultSet();
                while (RS.next()) {
                    WorldPub.pubs.add(RS.getString("data"));
                }
            }
        } catch (SQLException e) {
            sendError("PubData load", e);
        }
    }


    @Override
	public boolean update(Object t)	{
		return false;
	}
}
