package horizon.core.util;

import horizon.core.annotation.HorizonApplication;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class BasePackageScanner {

    public static String findBasePackage() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .forPackages("")
        );


        Set<Class<?>> candidates = reflections.getTypesAnnotatedWith(HorizonApplication.class);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("@HorizonApplication is not found in any class");
        }

        Class<?> baseClass = candidates.iterator().next();
        return baseClass.getPackage().getName();
    }
}