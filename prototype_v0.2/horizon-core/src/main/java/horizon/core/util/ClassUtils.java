/*
 * Copyright 2002-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package horizon.core.util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with {@link java.lang.Class} objects,
 * including operations to deal with class names, interfaces, assignability, etc.
 *
 * Mainly intended for internal framework usage.
 */
public abstract class ClassUtils {

    public static String getShortName(Class<?> clazz) {
        return getShortName(clazz.getName());
    }

    public static String getShortName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return (lastDotIndex != -1 ? className.substring(lastDotIndex + 1) : className);
    }

    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }

    public static String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }

    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            forName(className, classLoader);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(name, false, classLoader);
    }

    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        return (superType != null && subType != null && superType.isAssignableFrom(subType));
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return (clazz == Boolean.class || clazz == Byte.class || clazz == Character.class ||
                clazz == Double.class || clazz == Float.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Short.class || clazz == Void.class);
    }

    public static Class<?> getUserClass(Object instance) {
        return getUserClass(instance.getClass());
    }

    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz != null && clazz.getName().contains("$$")) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !Object.class.equals(superClass)) {
                return superClass;
            }
        }
        return clazz;
    }

    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new ArrayList<>();
        while (clazz != null) {
            for (Class<?> ifc : clazz.getInterfaces()) {
                if (!interfaces.contains(ifc)) {
                    interfaces.add(ifc);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        List<Class<?>> superclasses = new ArrayList<>();
        Class<?> current = clazz.getSuperclass();
        while (current != null && !Object.class.equals(current)) {
            superclasses.add(current);
            current = current.getSuperclass();
        }
        return superclasses;
    }

    public static boolean isConcrete(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }
}
