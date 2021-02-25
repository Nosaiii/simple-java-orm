package main.java.com.nosaiii.sjormtest;

import main.java.com.nosaiii.sjorm.ModelMetadata;
import main.java.com.nosaiii.sjorm.Query;
import main.java.com.nosaiii.sjorm.SJORM;

public class Main {
    public static void main(String[] args) {
        SJORM sjorm = SJORM.register("localhost", 3306, "gamemakerapitest", "root", "PixelKaasNL1");
        sjorm.registerModel(new ModelMetadata(Game.class));
        sjorm.registerModel(new ModelMetadata(Team.class));

        Query<Team> teams = sjorm.getAll(Team.class);
        for(Team team : teams.toList()) {
            team.setProperty("name", "TESTTT");
            team.save();
        }
    }
}