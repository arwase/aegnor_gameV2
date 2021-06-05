package database.statics.data;

import area.Area;
import client.Classe;
import com.zaxxer.hikari.HikariDataSource;
import database.statics.AbstractDAO;
import entity.Prism;
import game.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClasseData extends AbstractDAO<Classe> {
    public ClasseData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Classe classe)
    {
        return false;
    }

    public int load() {
        Result result = null;
        int numero = 0;
        try {
            result = getData("SELECT * from classes");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                World.world.addClasse(new Classe(RS.getInt("id"), RS.getString("name"),
                        RS.getString("gfxs"), RS.getString("size"), RS.getInt("mapInit")
                        , RS.getInt("cellInit"), RS.getInt("pdv")
                        , RS.getString("boostVita"), RS.getString("boostSage")
                        , RS.getString("boostForce"), RS.getString("boostIntel"), RS.getString("boostChance")
                        , RS.getString("boostAgi"), RS.getString("statsInit")
                        , RS.getString("sorts")));
                numero++;
            }
        } catch (SQLException e) {
            super.sendError("ClasseData load", e);
        } finally {
            close(result);
        }
        return numero;
    }
}
