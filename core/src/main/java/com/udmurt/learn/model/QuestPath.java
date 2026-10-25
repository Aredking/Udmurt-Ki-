package com.udmurt.learn.model;

import com.badlogic.gdx.utils.Array;

/**
 * Карта-путь: последовательность квестов, которые открываются по очереди.
 * Узел считается пройденным, когда выполнено его условие; следующий узел
 * становится доступным только после этого.
 */
public final class QuestPath {

    /** Условие выполнения узла. */
    public interface Goal {
        boolean done(User user);
    }

    public static class Node {
        public final String title;
        public final String description;
        /** Ключ рекомендуемой игры (см. QuestsScreen). */
        public final String gameKey;
        public final Goal goal;

        public Node(String title, String description, String gameKey, Goal goal) {
            this.title = title;
            this.description = description;
            this.gameKey = gameKey;
            this.goal = goal;
        }
    }

    private QuestPath() {
    }

    public static Array<Node> nodes() {
        Array<Node> list = new Array<>();
        list.add(new Node("Первые шаги", "Пройди первое занятие", "quiz",
                u -> u.lessons >= 1));
        list.add(new Node("Разминка", "Набери 15 звёзд", "picture",
                u -> u.stars >= 15));
        list.add(new Node("Память", "Найди 4 пары в «Найди пару»", "memory",
                u -> u.bestMemory >= 4));
        list.add(new Node("Пятёрка", "Пройди 5 занятий", "build",
                u -> u.lessons >= 5));
        list.add(new Node("Три дня", "Занимайся 3 дня подряд", "match",
                u -> u.streak >= 3));
        list.add(new Node("Полсотни", "Набери 50 звёзд", "sort",
                u -> u.stars >= 50));
        list.add(new Node("Без ошибок", "8 из 10 в викторине", "quiz",
                u -> u.bestQuiz >= 8));
        list.add(new Node("Сотня", "Набери 100 звёзд", "race",
                u -> u.stars >= 100));
        list.add(new Node("Неделя", "Занимайся 7 дней подряд", "sentence",
                u -> u.streak >= 7));
        list.add(new Node("Кругозор", "Пройди 20 занятий", "odd",
                u -> u.lessons >= 20));
        return list;
    }

    /** Номер первого непройденного узла (сколько узлов уже пройдено). */
    public static int completedCount(User user) {
        if (user == null) return 0;
        int done = 0;
        for (Node node : nodes()) {
            if (node.goal.done(user)) done++;
            else break;
        }
        return done;
    }
}
