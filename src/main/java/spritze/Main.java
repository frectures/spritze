package spritze;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    private static final ClassLoader classLoader = Main.class.getClassLoader();

    // MusikService.class -> MusikService instance
    // PartyService.class -> PartyService instance
    private static final HashMap<Class<?>, Object> components = new HashMap<>();

    public static void main(String[] args) throws Exception {
        initializeComponents();

        PartyService partyService = (PartyService) components.get(PartyService.class);
        partyService.party();
    }

    private static void initializeComponents() throws Exception {
        for (Class<?> clazz : scanClassPath()) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (annotation.annotationType() == Komponente.class) {
                    // 1. MusikService()           2. PartyService(MusikService)
                    var constructor = clazz.getConstructors()[0];
                    System.out.println(constructor);

                    // 1. { }                      2. { MusikService.class }
                    var parameterTypes = constructor.getParameterTypes();

                    // 1. { }                      2. { MusikService instance }
                    var arguments = Arrays.stream(parameterTypes).map(components::get).toArray();

                    // 1. MusikService instance    2. PartyService instance
                    var instance = constructor.newInstance(arguments);

                    components.put(clazz, instance);
                    System.out.println(clazz + " -> " + instance);
                    System.out.println();
                }
            }
        }
    }

    public static List<Class<?>> scanClassPath() throws Exception {

        // C:\Users\fred\git\frectures\spritze\target\classes
        Path root = Paths.get(classLoader.getResource("").toURI());

        // C:\Users\fred\git\frectures\spritze\target\classes\
        int rootLength1 = root.toString().length() + 1;

        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .map(Path::toString)
                    .filter(path -> path.endsWith(".class"))
                    // C:\Users\fred\git\frectures\spritze\target\classes\spritze\MusikService.class
                    .peek(System.out::println)
                    //                                                    spritze\MusikService
                    .map(path -> path.substring(rootLength1, path.length() - 6))
                    //                                                    spritze.MusikService
                    .map(path -> path.replace(File.separatorChar, '.'))
                    .flatMap(Main::tryLoadClass)
                    .toList();
        } finally {
            System.out.println();
        }
    }

    public static Stream<Class<?>> tryLoadClass(String name) {
        try {
            return Stream.of(Class.forName(name, false, classLoader));
        } catch (ClassNotFoundException ex) {
            return Stream.empty();
        }
    }
}
