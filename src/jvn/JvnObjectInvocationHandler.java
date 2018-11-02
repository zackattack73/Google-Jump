package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnObjectInvocationHandler implements InvocationHandler {
    private JvnObject jvnObject;

    private JvnObjectInvocationHandler(JvnObject object) {
        jvnObject = object;
    }

    /**
     * Look up the object with the name jon on the coord. If it does not exists,
     * then return null.
     *
     * @param jon the name of the object to search on the coord
     * @return the object associated with the name given, or null if not on the coord
     */
    public static Object lookup(String jon) {
        JvnLocalServer server = JvnServerImpl.jvnGetServer();

        try {
            JvnObject o = server.jvnLookupObject(jon);

            return newInstance(o);
        } catch (JvnException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create a new object locally and register it on the coord with
     * the name jon.
     *
     * @param serializable the object to create and register
     * @param jon          the name to associate with on the coord
     * @return the object created, null if an error occurred
     */
    public static Object create(Serializable serializable, String jon) {
        JvnLocalServer server = JvnServerImpl.jvnGetServer();

        try {
            JvnObject o = server.jvnCreateObject(serializable);
            o.jvnUnLock();
            server.jvnRegisterObject(jon, o);

            return newInstance(o);
        } catch (JvnException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object newInstance(JvnObject object) throws JvnException {
        if (object != null) {
            Serializable state = object.jvnGetObjectState();
            return Proxy.newProxyInstance(
                    state.getClass().getClassLoader(),
                    state.getClass().getInterfaces(),
                    new JvnObjectInvocationHandler(object));
        }

        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //System.out.println("Invoke " + method.getName() + " with " + (args != null ? args.length : 0) + " args.");

        if (method.isAnnotationPresent(JvnLockReadAnnotation.class)) {
            //System.out.println("PROXY: lockRead");
            jvnObject.jvnLockRead();
        } else if (method.isAnnotationPresent(JvnLockWriteAnnotation.class)) {
            //System.out.println("PROXY: lockWrite");
            jvnObject.jvnLockWrite();
        }

        Object result = method.invoke(jvnObject.jvnGetObjectState(), args);

        jvnObject.jvnUnLock();

        return result;
    }
}
