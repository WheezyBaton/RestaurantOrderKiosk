package com.wheezybaton.kiosk_system;

import com.wheezybaton.kiosk_system.dto.*;
import com.wheezybaton.kiosk_system.model.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimplePojoTest {

    @Test
    void testAllPojos() {
        testGetterSetter(new Order());
        testGetterSetter(new Product());
        testGetterSetter(new Ingredient());
        testGetterSetter(new Category());
        testGetterSetter(new CartSession());
        testGetterSetter(new OrderItem());
        testGetterSetter(new OrderItemModifier());
        testGetterSetter(new ProductIngredient());

        testGetterSetter(new CartItemDto());
        testGetterSetter(new CreateProductRequest());
        testGetterSetter(new ProductDto());
        testGetterSetter(new ProductIngredientDto());
        testGetterSetter(new SalesStatDto("Test", 1L, BigDecimal.TEN));
    }

    private void testGetterSetter(Object instance) {
        assertDoesNotThrow(() -> {
            Method[] methods = instance.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object value = getDefaultValue(paramType);
                    method.invoke(instance, value);
                }
            }
            for (Method method : methods) {
                if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
                        && method.getParameterCount() == 0) {
                    method.invoke(instance);
                }
            }
            instance.toString();
            instance.hashCode();
            instance.equals(new Object());
            instance.equals(instance);
        });
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == String.class) return "test";
        if (type == Long.class || type == long.class) return 1L;
        if (type == Integer.class || type == int.class) return 1;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == BigDecimal.class) return BigDecimal.ONE;
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == java.util.List.class) return new ArrayList<>();
        return null;
    }
}