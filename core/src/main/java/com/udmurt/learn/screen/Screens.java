package com.udmurt.learn.screen;

import com.badlogic.gdx.Screen;
import com.udmurt.learn.UdmurtGame;

/** Создаёт экраны игр по строковому ключу (для дорожки обучения и квестов). */
public final class Screens {

    private Screens() {
    }

    public static Screen of(UdmurtGame game, String key) {
        if (key == null) return new MainMenuScreen(game);
        switch (key) {
            case "learn": return new LearnScreen(game);
            case "picture": return new PictureGameScreen(game);
            case "build": return new BuildWordScreen(game);
            case "missing": return new MissingLetterScreen(game);
            case "memory": return new MemoryGameScreen(game);
            case "alphabet": return new AlphabetScreen(game);
            case "quiz": return new QuizScreen(game);
            case "sentence": return new SentenceBuilderScreen(game);
            case "match": return new MatchMadnessScreen(game);
            case "sort": return new SortCategoriesScreen(game);
            case "odd": return new OddOneOutScreen(game);
            case "race": return new TranslateRaceScreen(game);
            case "quests": return new QuestsScreen(game);
            case "leaderboard": return new LeaderboardScreen(game);
            default: return new MainMenuScreen(game);
        }
    }
}
