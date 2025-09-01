package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;

import java.time.Instant;
import java.util.*;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {

    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService; // Внедряем зависимость

    // Компаратор для сортировки постов по дате
    private final Comparator<Post> postDateComparator = Comparator.comparing(Post::getPostDate);


    // Конструктор с внедрением UserService
    public PostService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получает все посты с возможностью сортировки, пагинации
     *
     * @param sort  порядок сортировки (ASCENDING или DESCENDING)
     * @param from  количество пропускаемых элементов (для пагинации)
     * @param size  максимальное количество возвращаемых элементов
     * @return коллекция отсортированных постов
     */
    public Collection<Post> findAll(SortOrder sort, int from, int size) {
        return posts.values()           // Получаем все посты
                .stream()               // Создаем поток для обработки
                .sorted(sort.equals(SortOrder.ASCENDING) ?  // Сортируем по дате
                        postDateComparator :                // По возрастанию
                        postDateComparator.reversed())      // По убыванию
                .skip(from)             // Пропускаем первые 'from' элементов
                .limit(size)            // Ограничиваем количество результатов
                .toList();              // Преобразуем в список
    }

    public Post create(Post post) {
        // проверяем выполнение необходимых условий
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        // Проверяем, что автор поста существует
        if (post.getAuthorId() <= 0) {
            throw new ConditionsNotMetException("Автор поста должен быть указан");
        }

        // Используем метод findUserById для проверки существования пользователя
        if (userService.findUserById(post.getAuthorId()).isEmpty()) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден");
        }

        // формируем дополнительные данные
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        // проверяем необходимые условия
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    // Добавленный метод для поиска поста по ID
    public Optional<Post> findByPostId(@PathVariable Long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
