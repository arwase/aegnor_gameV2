package database.statics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import game.world.World;
import other.Titre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TitleData extends AbstractDAO<TitleData>  {

    public TitleData(HikariDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {}

    @Override
    public boolean update(TitleData obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM `titre`;");
            ResultSet RS = result.resultSet;

            while (RS.next()) {
                Titre titre = new Titre((RS.getInt("id")), (RS.getString("name")) , (RS.getInt("prix")),(RS.getString("conditions")) );
                World.world.addTitre(titre);

            }
        } catch (SQLException e) {
            super.sendError("Subarea_dataData load", e);
        } finally {
            close(result);
        }
    }
}

