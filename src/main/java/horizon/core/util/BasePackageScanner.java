package horizon.core.util;

import horizon.core.annotation.HorizonApplication;

import java.util.Set;

public class BasePackageScanner {

    public static String findBasePackage() {
        Reflections reflections = new Reflections("");

        Set<Class<?>> candidates = reflections.getTypesAnnotatedWith(HorizonApplication.class);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("@HorizonApplication이 붙은 클래스를 찾을 수 없습니다.");
        }

        Class<?> baseClass = candidates.iterator().next();
        return baseClass.getPackage().getName();
    }
}