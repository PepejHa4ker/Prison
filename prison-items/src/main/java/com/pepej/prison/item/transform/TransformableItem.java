package com.pepej.prison.item.transform;

import com.pepej.prison.item.transform.registry.TransformersRegistry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public interface TransformableItem {


    /**
     * Зарегистрировать трансформер для предмета
     * Обычно это делается через {@link TransformersRegistry#registerTransformerAdapter(TransformableItem, Class, TransformerAdapter)}
     * вызывается в конструкторе
     */
    default void registerTransformers() {}

    /**
     * Проверка на пустой предмет
     * В результате обработки предмета в выходной продукции может ничего не быть
     * в таком случае возвращается {@link TransformableItem#zero()} в качестве результата
     * @return true если предмет пустой
     */
    default boolean isEmpty() {
        return this == zero();
    }

    /**
     * Получить адаптер предмета для класса трансформера
     * @param transformClass класс трансформера
     * @return адаптер предмета для трансформера
     * @param <T> тип трансформера
     */
    default <T extends Transformer> TransformerAdapter<T> getAdapterFor(Class<T> transformClass) {
        return NOOPAdapter.INSTANCE;
    }


    /**
     * Пустой предмет. Получается, если в результате обработки предмета он ничего не вернул
     * Если в результате обработки предмета вернулась пустая коллекция - предмет не был обработан
     * В случае, если вернулся пустой предмен - предмет был обработан и мы ничего не получили
     * @return Пустой предмет
     */
    static TransformableItem zero() {
        return ZERO.INSTANCE;
    }


    final class ZERO implements TransformableItem {

        static final TransformableItem INSTANCE = new ZERO();

        private ZERO() {}

    }

    final class NOOPAdapter<T extends Transformer> implements TransformerAdapter<T> {

        static final NOOPAdapter INSTANCE = new NOOPAdapter();

        private NOOPAdapter() {}

        @Override
        public TransformableItem getItem() {
            return null;
        }

        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(final T transformer) {
            return Collections.emptyList();
        }

        @Override
        public Duration getTransformDuration(final T transformer) {
            return Duration.ZERO;
        }
    }
}


