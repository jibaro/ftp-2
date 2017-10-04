package org.xbib.io.ftp.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RunWith(Parameterized.class)
public class FTPMessagesTest {

    private static final Map<Class<?>, Object> INSTANCES;

    static {
        Map<Class<?>, Object> map = new HashMap<>();

        map.put(boolean.class, true);
        map.put(Boolean.class, true);

        map.put(char.class, 'A');
        map.put(Character.class, 'A');

        map.put(byte.class, (byte) 13);
        map.put(Byte.class, (byte) 13);
        map.put(short.class, (short) 13);
        map.put(Short.class, (short) 13);
        map.put(int.class, 13);
        map.put(Integer.class, 13);
        map.put(long.class, 13L);
        map.put(Long.class, 13L);
        map.put(float.class, 13F);
        map.put(Float.class, 13F);
        map.put(double.class, 13D);
        map.put(Double.class, 13D);

        map.put(String.class, "foobar");
        map.put(Class.class, Object.class);
        map.put(Object.class, "foobar");

        map.put(Collection.class, Collections.emptyList());
        map.put(List.class, Collections.emptyList());
        map.put(Set.class, Collections.emptySet());
        map.put(Map.class, Collections.emptyMap());

        map.put(OpenOption.class, StandardOpenOption.READ);
        map.put(OpenOption[].class, new OpenOption[0]);

        map.put(CopyOption.class, StandardCopyOption.REPLACE_EXISTING);
        map.put(CopyOption[].class, new CopyOption[0]);

        map.put(URI.class, URI.create("https://www.github.com/"));

        INSTANCES = Collections.unmodifiableMap(map);
    }

    private final Method method;
    private final Object target;

    public FTPMessagesTest(String testName, Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<>();
        collectParameters(parameters, FTPMessages.class, null, "Messages");
        return parameters;
    }

    private static void collectParameters(List<Object[]> parameters, Class<?> cls, Object instance, String path) {
        for (Method method : cls.getMethods()) {
            if (method.getDeclaringClass() != Object.class) {
                String methodPath = path + "." + method.getName();
                parameters.add(new Object[]{methodPath, method, instance});

                Class<?> returnType = method.getReturnType();
                if (returnType.getDeclaringClass() == FTPMessages.class) {
                    // a method that returns another messages providing object - recurse to that object if possible
                    // if not, then this test will fail
                    Object obj = Modifier.isStatic(method.getModifiers()) ? null : instance;
                    try {
                        Object[] arguments = getArguments(method);
                        Object returnValue = method.invoke(obj, arguments);
                        collectParameters(parameters, returnType, returnValue, methodPath);
                    } catch (NullPointerException | ReflectiveOperationException e) {
                        // ignore the exception; the test for the method will fail
                    }
                }
            }
        }
    }

    private static Object[] getArguments(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] arguments = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            arguments[i] = Objects.requireNonNull(INSTANCES.get(parameterTypes[i]), "no instance defined for " + parameterTypes[i]);
        }
        return arguments;
    }

    @Test
    public void testMethodCall() throws ReflectiveOperationException {
        Object obj = Modifier.isStatic(method.getModifiers()) ? null : target;
        Object[] args = getArguments(method);
        method.invoke(obj, args);
    }
}
