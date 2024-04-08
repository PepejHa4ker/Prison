package com.pepej.prison.item.transform;

import java.time.Duration;
import java.util.List;

/**
 * Интерфейс для регистрации способов обработки предмета разными механизмами
 * Для каждого конкретного вида механизма нужен отдельный адапатер для каждого предмета,
 * чтобы механизмы понимали, как обрабатывать этот предмет
 * @param <T> тип трансформера
 */
public interface TransformerAdapter<T extends Transformer> {

    /**
     * Возвращает обрабатываемый предмет
     * @return обрабатываемый предмет
     */
    TransformableItem getItem();

    /**
     * Обычно вызывается методом {@link Transformer#transform(TransformableItem)}
     * определяет, каков будет результат работы для конкретного трансформера
     * @param transformer трансформер который будет обрабатывать предмет
     * @return результат обработки предмета
     */
    List<TransformResultEntity<TransformableItem>> getResult(T transformer);

    /**
     * Необходимое кол-во топлива для обработки предмета конкретным механизмом
     * @param transformer трансформер который будет обрабатывать предме
     * @return стоимость обработки предмета
     */
    default int getFuelCostFor(T transformer) {
        return 0;
    }

    /**
     * Время обработки предмета конкретным механизмом
     * @param transformer трансформер который будет обрабаывать механизм
     * @return время обработки предмета
     */
    Duration getTransformDuration(T transformer);


}
