package com.udmurt.learn.model;

/** Учётная запись ребёнка. Логин — это ФИО, записанное русскими буквами. */
public class User {

    public String name;
    public String password;
    public Role role;
    public int stars;
    public int lessons;
    public int bestQuiz;
    public int bestMemory;
    /** Сколько дней подряд ребёнок занимался. */
    public int streak;
    /** Номер последнего дня (в сутках UTC) с занятием — для расчёта серии. */
    public int lastDay;

    // --- Ежедневные квесты -------------------------------------------------
    /** Номер дня (сутки UTC), к которому относится дневная статистика. */
    public int questDay;
    /** Сколько звёзд заработано сегодня. */
    public int dailyStars;
    /** Сколько занятий пройдено сегодня. */
    public int dailyGames;
    /** Пройдена ли сегодня игра без ошибок. */
    public boolean dailyPerfect;
    /** Забран ли приз за «звёздный» квест дня. */
    public boolean claimedStars;
    /** Забран ли приз за «игровой» квест дня. */
    public boolean claimedGames;
    /** Забран ли приз за «идеальный» квест дня. */
    public boolean claimedPerfect;

    public User() {
    }

    public User(String name, String passwordHash, Role role) {
        this.name = name;
        this.password = passwordHash;
        this.role = role;
        this.stars = 0;
    }

    /** Засчитывает пройденное занятие и обновляет серию дней подряд. */
    public void recordLesson() {
        lessons++;
        int today = today();
        if (lastDay == today) {
            return;
        }
        streak = lastDay == today - 1 ? streak + 1 : 1;
        lastDay = today;
    }

    /**
     * Завершает игру: обновляет общую статистику и дневной счётчик квестов.
     *
     * @param starsEarned заработанные за игру звёзды
     * @param perfect     пройдено ли занятие без ошибок
     */
    public void finishRun(int starsEarned, boolean perfect) {
        rollDay();
        dailyStars += starsEarned;
        dailyGames++;
        if (perfect) dailyPerfect = true;
        addStars(starsEarned);
        recordLesson();
    }

    public void addStars(int amount) {
        stars = Math.max(0, stars + amount);
    }

    /** Сбрасывает дневную статистику, если наступил новый день. */
    public void ensureToday() {
        rollDay();
    }

    private void rollDay() {
        int today = today();
        if (questDay != today) {
            questDay = today;
            dailyStars = 0;
            dailyGames = 0;
            dailyPerfect = false;
            claimedStars = false;
            claimedGames = false;
            claimedPerfect = false;
        }
    }

    private static int today() {
        return (int) (System.currentTimeMillis() / 86400000L);
    }

    public String firstName() {
        if (name == null || name.isBlank()) return "друг";
        return name.trim().split("\\s+")[0];
    }
}
