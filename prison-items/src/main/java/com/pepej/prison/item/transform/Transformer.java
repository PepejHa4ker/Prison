package com.pepej.prison.item.transform;

import java.time.Duration;
import java.util.List;

/**
 * Интерфейс для описания поведения разных трансформеров (не оптимус прайм)
 */
public interface Transformer {

    /**
     * Трансформировать предмет
     * @param item предмет для трансформации
     * @return результать трансформации предмета
     */
    List<TransformResultEntity<TransformableItem>> transform(TransformableItem item);

    /**
     * Уникальное имя трансформера
     * @return уникальное имя трансформатора
     */
    String getName();

    /**
     * Текущий прогресс трансформации предмета
     * @param item трансформируемый предмет
     * @return текущий прогресс трансформации предмета
     */
    double getTransformProgressFor(TransformableItem item);

    /**
     * Множитель скорости работы трансформера
     * @return множетиль скорости работы
     */
    double getSpeedMultiplier();

    /**
     * Установить текущий процесс трансформации предмета
     * @param item трансформируемый предмет
     * @param progress прогресс который будет установлен
     */
    void setTransformProgressFor(TransformableItem item, double progress);

    /**
     * Время трансформации предмета
     * @param item трансформируемый предмет
     * @return время трансформации предмета
     */
    Duration getTransformDurationFor(TransformableItem item);

    /**
     * Множитель выходной продукции у трансформера
     * @return множитель выходной продукции у трансформера
     */
    default double getProductionMultiplier() {
        return 1.0;
    }
}
