package org.conscrypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Proxy to classes and method by reflection, whilst harmlessly doing nothing or returning default
 * values if the class or method is not present. Designed to be inherited to produce
 * type-safe proxies.
 * <p>
 * Throws when detecting various mis-configurations, but these checks should trigger during
 * development rather than in production.
 */
public class ClassProxy {
    private final Class<?> myClass;

    ClassProxy(String className) {
        Class<?> clss;
        try {
            clss = Class.forName(className);
        } catch (ClassNotFoundException e) {
            clss = null;
        }
        myClass = clss;
    }

    ClassProxy(Class<?> clss) {
        myClass = clss;
    }

    public boolean isPresent() {
        return myClass != null;
    }

    /**
     * Returns a method declared in this class or null if no such method exists.
     * Class signature must match the name and supplied parameter types.
     * Methods may have any visibility but inherited classes will be excluded.
     * If the method is not accessible then try and make it so.
     */
    public MethodProxy getDeclared(String methodName, Class<?>... types) {
        try {
            if (isPresent()) {
                MethodProxy proxy = new MethodProxy(myClass.getDeclaredMethod(methodName, types));
                if (proxy.isAccessible()) {
                    proxy.setAccessible(true);
                }
                return proxy;
            }
        } catch (NoSuchMethodException e) {
            // Fall through
        }
        return new Noop();
    }

    /**
     * Returns a method available in this class or null if no such method exists.
     * Class signature must match the name and supplied parameter types.
     * Inherited classes will be included, but only public methods will be returned.
     */
    public MethodProxy getMethod(String methodName, Class<?>... types) {
        try {
            if (isPresent()) {
                // Should already be public
                return new MethodProxy(myClass.getMethod(methodName, types));
            }
        } catch (NoSuchMethodException e) {
            // Fall through
        }
        return new Noop();
    }

    public static class MethodProxy {
        private final Method method;
        private final boolean isStatic;
        private Object defaultValue = null;
        private Class<?> returnType = null;

        MethodProxy(Method method) {
            this.method = method;
            isStatic = isPresent() && Modifier.isStatic(method.getModifiers());
        }

        public boolean isPresent() {
            return method != null;
        }

        @SuppressWarnings("deprecation")
        public boolean isAccessible() {
            if (!isPresent()) {
                // No-op is always accesible.
                return true;
            }
            return method.isAccessible();
        }

        public void setAccessible(boolean flag) {
            if (isPresent()) {
                method.setAccessible(flag);
            }
        }

        public MethodProxy setReturnType(Class<?> expected) {
            if (isPresent()) {
                Class<?> returnType = method.getReturnType();
                if (!expected.isAssignableFrom(returnType)) {
                    throw new IllegalArgumentException("Return type mismatch");
                }
            }
            this.returnType = expected;
            return this;
        }

        // Invokes an instance method and rethrows any checked exceptions as the correct type.
        public Object invokeChecked(Object receiver, Object... args) throws Throwable {
            if (isPresent()) {
                Preconditions.checkState(!this.isStatic,
                        "Static method called with receiver");
                try {
                    return method.invoke(receiver, args);
                } catch (IllegalAccessException e) {
                    // Fall through and return default value.
                } catch (InvocationTargetException exception) {
                    rethrowChecked(exception.getCause());
                }
            }
            return getDefaultValue();
        }

        // Invokes a static method and rethrows any checked exceptions as the correct type.
        public Object invokeStaticChecked(Object... args) throws Throwable {
            if (isPresent()) {
                Preconditions.checkState(this.isStatic,
                        "Instance method called statically");
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException e) {
                    // Fall through and return default value.
                } catch (InvocationTargetException exception) {
                    rethrowChecked(exception.getCause());
                }
            }
            return getDefaultValue();
        }

        // Invokes an instance method and wraps any checked exceptions in a RuntimeException.
        public Object invoke(Object receiver, Object... args) {
            if (isPresent()) {
                Preconditions.checkState(!this.isStatic,
                        "Static method called with receiver");
                try {
                    return method.invoke(receiver, args);
                } catch (IllegalAccessException e) {
                    // Fall through and return default value.
                } catch (InvocationTargetException exception) {
                    rethrowUnchecked(exception.getCause());
                }
            }
            return getDefaultValue();
        }

        // Invokes a static method and wraps any checked exceptions in a RuntimeException.
        public Object invokeStatic(Object... args) {
            if (isPresent()) {
                Preconditions.checkState(this.isStatic,
                        "Instance method called statically");
                try {
                    return method.invoke(null, args);
                } catch (IllegalAccessException e) {
                    // Fall through and return default value.
                } catch (InvocationTargetException exception) {
                    rethrowUnchecked(exception.getCause());
                }
            }
            return getDefaultValue();
        }

        private void rethrowUnchecked(Throwable cause) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }

        private void rethrowChecked(Throwable cause) throws Throwable {
            if (cause instanceof Exception) {
                Exception exception = (Exception) cause;
                if (exception instanceof RuntimeException || isExceptionOurs(exception)) {
                    throw exception;
                }
                throw new IllegalStateException("Unexpected checked exception", exception);
            } else {
                throw cause;
            }
        }

        private boolean isExceptionOurs(Exception exception) {
            for (Class<?> chk : method.getExceptionTypes()) {
                if (chk.isAssignableFrom(exception.getClass())) {
                    return true;
                }
            }
            return false;
        }

        private Object getDefaultValue() {
            return defaultValue;
        }

        public MethodProxy setDefaultValue(Object defaultValue) {
            Preconditions.checkState(returnType != null,
                    "Must set a return type if setting default");
            // This is a bit ugly but primitive types don't know about autoboxing so always
            // return false for isAssignableFrom any other type, so we can't check and
            // just have to trust the caller.
            if (returnType.isPrimitive() || returnType.isAssignableFrom(defaultValue.getClass())) {
                this.defaultValue = defaultValue;
                return this;
            }
            throw new IllegalStateException("Default value incompatible with return type");
        }
    }

    private static class Noop extends MethodProxy {
        Noop() {
            super(null);
        }
    }
}
