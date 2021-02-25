package main.java.com.nosaiii.sjormtest;

import main.java.com.nosaiii.sjorm.ModelMetadata;
import main.java.com.nosaiii.sjorm.SJORM;

public class Main {
    public static void main(String[] args) {
        SJORM sjorm = SJORM.register("127.0.0.1", 3306, "gamemakerapitest", "root", "PixelKaasNL1");
        sjorm.registerModel(new ModelMetadata(Game.class));

        // Create a new model instance
        Game game = new Game();

        // Set its required properties
        game.setProperty("name", "New super awesome game");
        game.setProperty("min_players", 8);
        game.setProperty("max_players", 24);

        game.save();
    }
}