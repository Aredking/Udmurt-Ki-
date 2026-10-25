package com.udmurt.learn.data;

import java.util.LinkedHashSet;
import java.util.Set;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.udmurt.learn.model.Role;

/**
 * Словарь удмуртских слов с переводом и картинкой.
 *
 * Слова помечены как «дошкольные» (simple) или «школьные» (school) — в
 * зависимости от выбора ребёнка меняются набор слов, вопросы и задания.
 *
 * ВНИМАНИЕ: словарный запас собран для демонстрации приложения. Перед
 * реальным использованием переводы стоит проверить у носителя языка.
 *
 * Картинки — OpenMoji (CC BY-SA 4.0), .png из официального релиза,
 * лежат в assets/images/ и называются по hex-кодовой точке эмодзи.
 */
public final class WordBank {

    public static final String ANIMALS = "Животные";
    public static final String FOOD = "Еда";
    public static final String NATURE = "Природа";
    public static final String FAMILY = "Семья";
    public static final String BODY = "Тело";
    public static final String HOME = "Дом";
    public static final String COLORS = "Цвета";
    public static final String NUMBERS = "Числа";
    public static final String SEASONS = "Времена года";
    public static final String SCHOOL = "Школа";

    public static final Array<Word> ALL = new Array<>();

    private WordBank() {
    }

    private static void add(String udmurt, String russian, String image, String category, boolean school) {
        ALL.add(new Word(udmurt, russian, image, category, school));
    }

    private static void addBoth(String udmurt, String russian, String image, String category) {
        ALL.add(new Word(udmurt, russian, image, category, false));
    }

    private static void addSchool(String udmurt, String russian, String image, String category) {
        ALL.add(new Word(udmurt, russian, image, category, true));
    }

    static {
        // Животные
        addBoth("кочыш", "кошка", "images/1F408.png", ANIMALS);
        addBoth("кучо", "собака", "images/1F415.png", ANIMALS);
        addBoth("вал", "лошадь", "images/1F40E.png", ANIMALS);
        addBoth("скал", "корова", "images/1F404.png", ANIMALS);
        addBoth("парсь", "свинья", "images/1F416.png", ANIMALS);
        addBoth("курег", "курица", "images/1F414.png", ANIMALS);
        addBoth("чорыг", "рыба", "images/1F41F.png", ANIMALS);
        addSchool("атас", "петух", "images/1F413.png", ANIMALS);
        addSchool("ыж", "овца", "images/1F411.png", ANIMALS);
        addSchool("гондыр", "медведь", "images/1F43B.png", ANIMALS);
        addSchool("кион", "волк", "images/1F43A.png", ANIMALS);
        addSchool("ӟичы", "лиса", "images/1F98A.png", ANIMALS);
        addSchool("кунь", "заяц", "images/1F407.png", ANIMALS);
        addSchool("тылобурдо", "птица", "images/1F426.png", ANIMALS);

        // Еда
        addBoth("нянь", "хлеб", "images/1F35E.png", FOOD);
        addBoth("йӧл", "молоко", "images/1F95B.png", FOOD);
        addBoth("ву", "вода", "images/1F4A7.png", FOOD);
        addSchool("силь", "мясо", "images/1F969.png", FOOD);
        addSchool("сылал", "соль", "images/1F9C2.png", FOOD);
        addSchool("вӧй", "масло", "images/1F9C8.png", FOOD);
        addSchool("пуз", "яйцо", "images/1F95A.png", FOOD);

        // Природа
        addBoth("шунды", "солнце", "images/2600.png", NATURE);
        addBoth("толэзь", "луна", "images/1F319.png", NATURE);
        addBoth("кизили", "звезда", "images/2B50.png", NATURE);
        addBoth("писпу", "дерево", "images/1F333.png", NATURE);
        addBoth("сяська", "цветок", "images/1F338.png", NATURE);
        addBoth("лымы", "снег", "images/2744.png", NATURE);
        addSchool("турын", "трава", "images/1F33F.png", NATURE);
        addSchool("зор", "дождь", "images/1F327.png", NATURE);
        addSchool("тыл", "огонь", "images/1F525.png", NATURE);
        addSchool("шур", "река", "images/1F3DE.png", NATURE);
        addSchool("гурезь", "гора", "images/26F0.png", NATURE);
        addSchool("му", "земля", "images/1F30D.png", NATURE);
        addSchool("ин", "небо", "images/1F30C.png", NATURE);

        // Семья
        addBoth("анай", "мама", "images/1F469.png", FAMILY);
        addBoth("атай", "папа", "images/1F468.png", FAMILY);
        addBoth("пинал", "ребёнок", "images/1F476.png", FAMILY);
        addSchool("песянай", "бабушка", "images/1F475.png", FAMILY);
        addSchool("песятай", "дедушка", "images/1F474.png", FAMILY);
        addSchool("пи", "сын", "images/1F466.png", FAMILY);
        addSchool("ныл", "дочь", "images/1F467.png", FAMILY);

        // Тело
        addBoth("син", "глаз", "images/1F441.png", BODY);
        addBoth("пель", "ухо", "images/1F442.png", BODY);
        addBoth("ныр", "нос", "images/1F443.png", BODY);
        addBoth("ым", "рот", "images/1F444.png", BODY);
        addBoth("ки", "рука", "images/270B.png", BODY);
        addBoth("сюлэм", "сердце", "images/2764.png", BODY);
        addBoth("кыл", "язык", "images/1F445.png", BODY);
        addSchool("пыд", "нога", "images/1F9B6.png", BODY);
        addSchool("пинь", "зуб", "images/1F9B7.png", BODY);

        // Дом
        addBoth("корка", "дом", "images/1F3E0.png", HOME);
        addBoth("ӧс", "дверь", "images/1F6AA.png", HOME);
        addBoth("пукон", "стул", "images/1FA91.png", HOME);
        addBoth("книга", "книга", "images/1F4DA.png", HOME);
        addBoth("пуньы", "ложка", "images/1F944.png", HOME);
        addSchool("пурт", "нож", "images/1F52A.png", HOME);

        // Цвета (без картинок — используются в учёбе и викторине)
        addBoth("горд", "красный", null, COLORS);
        addBoth("тӧдьы", "белый", null, COLORS);
        addBoth("сьӧд", "чёрный", null, COLORS);
        addBoth("лыз", "синий", null, COLORS);
        addBoth("вож", "зелёный", null, COLORS);
        addBoth("ӵуж", "жёлтый", null, COLORS);

        // Числа
        addBoth("одӥг", "один", null, NUMBERS);
        addBoth("кык", "два", null, NUMBERS);
        addBoth("куинь", "три", null, NUMBERS);
        addBoth("ньыль", "четыре", null, NUMBERS);
        addBoth("вить", "пять", null, NUMBERS);
        addBoth("куать", "шесть", null, NUMBERS);
        addBoth("сизьым", "семь", null, NUMBERS);
        addBoth("тямыс", "восемь", null, NUMBERS);
        addBoth("укмыс", "девять", null, NUMBERS);
        addBoth("дас", "десять", null, NUMBERS);

        // Времена года и время суток
        addBoth("тол", "зима", null, SEASONS);
        addBoth("тулыс", "весна", null, SEASONS);
        addBoth("гужем", "лето", null, SEASONS);
        addBoth("сӥзьыл", "осень", null, SEASONS);
        addBoth("нунал", "день", null, SEASONS);
        addBoth("уй", "ночь", null, SEASONS);

        // Школа (только для школьников)
        addSchool("дышетӥсь", "учитель", null, SCHOOL);
        addSchool("дышетскись", "ученик", null, SCHOOL);
        addSchool("дышетсконни", "школа", null, SCHOOL);
        addSchool("буква", "буква", null, SCHOOL);
        addSchool("кылбугор", "слово", null, SCHOOL);
    }

    /** Слова, доступные ребёнку с выбранной ролью. */
    public static Array<Word> forRole(Role role) {
        Array<Word> result = new Array<>();
        for (Word word : ALL) {
            if (role == Role.SCHOOL || !word.school) {
                result.add(word);
            }
        }
        return result;
    }

    /** Категории, доступные ребёнку с выбранной ролью. */
    public static Array<String> categories(Role role) {
        Set<String> set = new LinkedHashSet<>();
        for (Word word : ALL) {
            if (role == Role.SCHOOL || !word.school) {
                set.add(word.category);
            }
        }
        return new Array<>(set.toArray(new String[0]));
    }

    public static Array<Word> byCategory(String category, Role role) {
        Array<Word> result = new Array<>();
        for (Word word : ALL) {
            if (word.category.equals(category) && (role == Role.SCHOOL || !word.school)) {
                result.add(word);
            }
        }
        return result;
    }

    /** Слова с картинкой (для игры «картинка → слово»). */
    public static Array<Word> withPicture(Role role) {
        Array<Word> result = new Array<>();
        for (Word word : forRole(role)) {
            if (word.hasPicture()) result.add(word);
        }
        return result;
    }

    /** Случайные «похожие» слова для вариантов ответа. */
    public static Array<Word> distractors(Word correct, Array<Word> pool, int count) {
        Array<Word> candidates = new Array<>();
        for (Word word : pool) {
            if (!word.udmurt.equals(correct.udmurt)) candidates.add(word);
        }
        candidates.shuffle();
        Array<Word> result = new Array<>();
        for (int i = 0; i < count && i < candidates.size; i++) {
            result.add(candidates.get(i));
        }
        return result;
    }

    /** Перемешать варианты ответа так, чтобы правильный был в случайном месте. */
    public static <T> Array<T> shuffled(Array<T> source) {
        Array<T> copy = new Array<>(source);
        copy.shuffle();
        return copy;
    }

    public static <T> T random(Array<T> source) {
        if (source.size == 0) return null;
        return source.get(MathUtils.random(source.size - 1));
    }
}