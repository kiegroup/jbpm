package org.jbpm.formbuilder.common.reflect;


import com.gwtent.reflection.client.ClassType;
import com.gwtent.reflection.client.Constructor;
import com.gwtent.reflection.client.TypeOracle;

public class ReflectionHelper {

    public static Object newInstance(String klass) throws Exception {
        ClassType<?> classType = TypeOracle.Instance.getClassType(klass);
        Constructor<?> constructor = classType.findConstructor();
        return constructor.newInstance();
        /*Class<?> clazz = Class.forName(klass);
        Constructor<?> c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();*/
    }
}
