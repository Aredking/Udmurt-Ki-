package com.udmurt.learn.model;

import com.badlogic.gdx.utils.Array;

/** Список достижений ребёнка и проверка, какие из них уже получены. */
public final class Achievements {

    public static class Badge {
        public final String name;
        public final String description;
        public final boolean earned;

        public Badge(String name, String description, boolean earned) {
            this.name = name;
            this.description = description;
            this.earned = earned;
        }
    }

    private Achievements() {
    }

    public static Array<Badge> forUser(User user) {
        Array<Badge> list = new Array<>();
        if (user == null) return list;

        list.add(new Badge("Первые шаги", "Набрать 5 звёзд", user.stars >= 5));
        list.add(new Badge("Старт", "Пройти 1 занятие", user.lessons >= 1));
        list.add(new Badge("Звёздный путь", "Набрать 25 звёзд", user.stars >= 25));
        list.add(new Badge("Старательный ученик", "Пройти 5 занятий", user.lessons >= 5));
        list.add(new Badge("Эрудит", "8 из 10 в викторине", user.bestQuiz >= 8));
        list.add(new Badge("Мастер памяти", "Рекорд 6 ★ в «Найди пару»", user.bestMemory >= 6));
        list.add(new Badge("Три дня подряд", "Серия занятий 3 дня", user.streak >= 3));
        list.add(new Badge("Сотня", "Набрать 100 звёзд", user.stars >= 100));
        list.add(new Badge("Знаток", "Пройти 20 занятий", user.lessons >= 20));
        list.add(new Badge("Неделя силы", "Серия занятий 7 дней", user.streak >= 7));
        return list;
    }

    public static int earnedCount(User user) {
        int count = 0;
        for (Badge badge : forUser(user)) {
            if (badge.earned) count++;
        }
        return count;
    }
}
