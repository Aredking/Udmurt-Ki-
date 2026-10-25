package com.udmurt.learn.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.udmurt.learn.model.Role;
import com.udmurt.learn.model.User;

/**
 * Простое локальное хранилище аккаунтов в JSON-файле.
 * На Android/iOS файл лежит в приватной папке приложения,
 * на ПК — рядом с приложением.
 */
public class UserStore {

    private static final String FILE_NAME = "udmurt_users.json";

    public static class Data {
        public Array<User> users = new Array<>();
    }

    private final Array<User> users = new Array<>();
    private final Json json = new Json();
    private final FileHandle file;

    public UserStore() {
        file = Gdx.files.local(FILE_NAME);
        load();
    }

    private void load() {
        try {
            if (file.exists()) {
                Data data = json.fromJson(Data.class, file.readString("UTF-8"));
                if (data != null && data.users != null) {
                    for (User user : data.users) {
                        if (user.role == null) user.role = Role.PRESCHOOL;
                    }
                    users.addAll(data.users);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("UserStore", "Не удалось прочитать список пользователей", e);
        }
    }

    public void save() {
        try {
            Data data = new Data();
            data.users = users;
            file.writeString(json.prettyPrint(data), false, "UTF-8");
        } catch (Exception e) {
            Gdx.app.error("UserStore", "Не удалось сохранить пользователей", e);
        }
    }

    public boolean exists(String name) {
        if (name == null) return false;
        for (User user : users) {
            if (user.name != null && user.name.equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    public User authenticate(String name, String password) {
        if (name == null || password == null) return null;
        String hash = Passwords.hash(password);
        for (User user : users) {
            if (user.name != null
                    && user.name.equalsIgnoreCase(name.trim())
                    && hash.equals(user.password)) {
                return user;
            }
        }
        return null;
    }

    public User register(String name, String password, Role role) {
        User user = new User(name.trim(), Passwords.hash(password), role);
        users.add(user);
        save();
        return user;
    }

    /** Все локальные аккаунты (копия списка — менять её не влияет на хранилище). */
    public Array<User> all() {
        return new Array<>(users);
    }

    /**
     * Сохраняет аккаунт, подтверждённый сервером: создаёт локальную запись,
     * если её ещё нет, иначе обновляет хэш пароля (пароль проверен сервером).
     */
    public User upsert(String name, String password, Role role) {
        String trimmed = name.trim();
        String hash = Passwords.hash(password);
        for (User user : users) {
            if (user.name != null && user.name.equalsIgnoreCase(trimmed)) {
                if (!hash.equals(user.password)) {
                    user.password = hash;
                    save();
                }
                if (role != null) user.role = role;
                return user;
            }
        }
        User user = new User(trimmed, hash, role);
        users.add(user);
        save();
        return user;
    }

    public void update(User user) {
        save();
    }
}
