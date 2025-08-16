package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        // проверяем, что email не используется другим пользователем
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // Проверка 1: Id должен быть указан
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        // Проверка 2: Пользователь с таким id должен существовать
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User existingUser = users.get(newUser.getId());

        // Проверка 3: Email не должен дублировать чужой email
        if (newUser.getEmail() != null) {
            // Проверяем, есть ли другой пользователь с таким email
            for (User user : users.values()) {
                if (!user.getId().equals(newUser.getId()) && user.getEmail().equals(newUser.getEmail())) {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                }
            }
        }

        // Проверка 4: Обновляем только те поля, которые не равны null
        if (newUser.getEmail() != null) {
            existingUser.setEmail(newUser.getEmail());
        }

        if (newUser.getUsername() != null) {
            existingUser.setUsername(newUser.getUsername());
        }

        if (newUser.getPassword() != null) {
            existingUser.setPassword(newUser.getPassword());
        }

        return existingUser;
    }


    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
