package ru.yandex.practicum.catsgram.model;

/**
 * Перечисление для определения порядка сортировки
 */
public enum SortOrder {
    /** По возрастанию */
    ASCENDING,

    /** По убыванию */
    DESCENDING;

    /**
     * Преобразует строковое значение в элемент перечисления
     *
     * @param order строковое представление порядка сортировки
     * @return соответствующий элемент SortOrder или null, если не найден
     */
    public static SortOrder from(String order) {
        switch (order.toLowerCase()) {
            case "ascending":
            case "asc":
                return ASCENDING;       // Возвращаем сортировку по возрастанию
            case "descending":
            case "desc":
                return DESCENDING;      // Возвращаем сортировку по убыванию
            default:
                return null;            // Неверный параметр сортировки
        }
    }
}
