package ru.yandex.practicum.catsgram.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;
import ru.yandex.practicum.catsgram.service.PostService;

import java.util.Collection;
import java.util.Optional;


@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Получает список всех постов с параметрами сортировки и пагинации
     *
     * @param sort  порядок сортировки: "asc"/"ascending" или "desc"/"descending"
     * @param from  с какого поста начинать (по умолчанию 0)
     * @param size  сколько постов возвращать (по умолчанию 10)
     * @return коллекция постов
     *
     * Примеры запросов:
     * GET /posts                     - по умолчанию: последние 10 постов
     * GET /posts?sort=asc&from=5&size=3  - первые 3 поста, начиная с 5-го
     */
    @GetMapping
    public Collection<Post> findAll(
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        // Проверка параметра sort
        SortOrder sortOrder = SortOrder.from(sort);
        if (sortOrder == null) {
            throw new ParameterNotValidException("sort", "Получено: " + sort + " должно быть: asc или desc");
        }

        // Проверка параметра size
        if (size <= 0) {
            throw new ParameterNotValidException("size", "Размер должен быть больше нуля");
        }

        // Проверка параметра from
        if (from < 0) {
            throw new ParameterNotValidException("from", "Начало выборки должно быть положительным числом");
        }

        // Вызываем сервисный метод
        return postService.findAll(sortOrder, from, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }

    @GetMapping("/{postId}")
    public Optional<Post> findById(@PathVariable long postId) {
        return postService.findByPostId(postId);
    }
}