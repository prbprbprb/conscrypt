package org.conscrypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.conscrypt.ClassProxy.MethodProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.NoSuchAlgorithmException;

@RunWith(JUnit4.class)
public class ClassProxyTest {
    private static final String DEFAULT = "default";
    @Test
    public void findClass() {
        assertTrue(new ClassProxy("java.lang.String").isPresent());
        assertFalse(new ClassProxy("java.lang.Gnirts").isPresent());
    }

    @Test
    public void findMethod_classPresent() {
        ClassProxy classProxy = new ClassProxy("java.lang.String");
        MethodProxy missing = classProxy.getDeclared("lengthxxxxx");
        assertFalse(missing.isPresent());

        MethodProxy length
                = classProxy.getDeclared("length");
        assertTrue(length.isPresent());
        assertEquals(6, length.invoke("banana"));

        MethodProxy substring
                = classProxy.getDeclared("substring", int.class, int.class);
        assertTrue(substring.isPresent());
        assertEquals("ban", substring.invoke("banana", 0, 3));
    }

    @Test
    public void findMethod_classAbsent() {
        ClassProxy classProxy = new ClassProxy("java.lang.Gnirts");
        assertFalse(classProxy.isPresent());

        MethodProxy methodProxy = classProxy.getMethod("foo")
                .setReturnType(String.class);
        methodProxy.setDefaultValue(DEFAULT);
        assertFalse(methodProxy.isPresent());
        assertEquals(DEFAULT, methodProxy.invoke(null));

        methodProxy = classProxy.getDeclared("foo")
                .setReturnType(String.class);
        methodProxy.setDefaultValue(DEFAULT);
        assertFalse(methodProxy.isPresent());
        assertEquals(DEFAULT, methodProxy.invoke(null));
    }

    @Test
    public void returnType() {
        ClassProxy classProxy = new ClassProxy("java.lang.String");
        MethodProxy methodProxy = classProxy.getDeclared("xxxxxxxxx");

        assertThrows(IllegalStateException.class,
                () -> methodProxy.setDefaultValue(7));

        methodProxy.setReturnType(Integer.class);
        assertThrows(IllegalStateException.class,
                () -> methodProxy.setDefaultValue(DEFAULT)); // DEFAULT is a String
        methodProxy.setDefaultValue(7);
        assertEquals(7, methodProxy.invoke(null));
    }

    @Test
    public void returnType_invokeUnchecked() {
        ClassProxy classProxy = new ClassProxy("java.lang.String");

        MethodProxy substring
                = classProxy.getDeclared("substring", int.class, int.class)
                .setReturnType(String.class);
        assertTrue(substring.isPresent());
        assertEquals("ban", substring.invoke("banana", 0, 3));

        MethodProxy failing
                = classProxy.getDeclared("substring", int.class, int.class);
        assertThrows(IllegalArgumentException.class,
                () -> failing.setReturnType(Integer.class));
        assertTrue(substring.isPresent());
        // Method still returns original type.
        assertEquals("ban", failing.invoke("banana", 0, 3));
    }

    @Test
    public void returnType_invokeChecked() throws Throwable {
        ClassProxy classProxy = new ClassProxy("java.lang.String");

        MethodProxy substring
                = classProxy.getDeclared("substring", int.class, int.class)
                .setReturnType(String.class);
        assertTrue(substring.isPresent());
        assertEquals("ban", substring.invoke("banana", 0, 3));

        MethodProxy failing
                = classProxy.getDeclared("substring", int.class, int.class);
        assertThrows(IllegalArgumentException.class,
                () -> failing.setReturnType(Integer.class));
        assertTrue(substring.isPresent());
        // Method still returns original type.
        assertEquals("ban", failing.invokeChecked("banana", 0, 3));
    }

    @Test
    public void defaultReturn() throws Throwable {
        MethodProxy method =
                new ClassProxy("java.lang.String").getDeclared("xxxxxxxxx");
        assertNotNull(method);
        assertNull(method.invoke(""));
        assertNull(method.invokeChecked(""));

        method.setReturnType(String.class).setDefaultValue(DEFAULT);
        assertEquals(DEFAULT, method.invoke(""));
        assertEquals(DEFAULT, method.invokeChecked(""));
    }

    @Test
    public void checkedVsUnchecked() {
        MethodProxy getInstance =
                new ClassProxy("javax.crypto.Cipher")
                        .getDeclared("getInstance", String.class);
        assertNotNull(getInstance);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> getInstance.invokeStatic("XXXXXXXXX"));
        assertTrue(e.getCause() instanceof NoSuchAlgorithmException);

        assertThrows(NoSuchAlgorithmException.class,
                () -> getInstance.invokeStaticChecked("XXXXXXXXX"));
    }

    private static class StringProxy extends ClassProxy {
        private final MethodProxy length;
        private final MethodProxy substring;
        private final MethodProxy valueOfInt;
        private final String receiver;

        public StringProxy(String receiver) {
            super("java.lang.String");
            length = getDeclared("length")
                    .setReturnType(int.class)
                    .setDefaultValue(0);
            substring = getDeclared("substring", int.class, int.class);
            valueOfInt = getDeclared("valueOf", int.class);
            this.receiver = receiver;
        }

        public int length() {
            return (int) length.invoke(receiver);
        }

        public String substring(int start, int len) {
            return (String) substring.invoke(receiver, start, len);
        }

        public String valueOf(int value) {
            return (String) valueOfInt.invokeStatic(value);
        }
    }

    @Test
    public void proxyClass() {
        String input = "banana";
        StringProxy proxy = new StringProxy(input);

        assertEquals(input.length(), proxy.length());
        assertEquals(input.substring(1, 3), proxy.substring(1, 3));
        assertEquals("7", proxy.valueOf(7));

    }

    private static class TestBase {
        public String basePublicMethod() {
            return "Animal";
        }

        protected String baseProtectedMethod() {
            return "Mineral";
        }
    }

    private static class TestDerived extends TestBase {
        public String derivedMethod() {
            return "Vegetable";
        }
    }

    @Test
    public void nullReceiver() {
        ClassProxy classProxy = new ClassProxy(TestDerived.class);
        MethodProxy method = classProxy.getDeclared("derivedMethod");
        assertTrue(method.isPresent());
        assertThrows(NullPointerException.class, () -> method.invoke(null));
        assertThrows(NullPointerException.class, () -> method.invokeChecked(null));
    }

    @Test
    public void staticMethod() {
        ClassProxy classProxy = new ClassProxy(String.class);
        MethodProxy method = classProxy.getDeclared("valueOf", int.class);
        assertTrue(method.isPresent());
        assertThrows(IllegalStateException.class, () -> method.invoke("", 2));
        assertThrows(IllegalStateException.class, () -> method.invokeChecked("", 2));
    }

    @Test
    public void methodFinding_Declared() {
        ClassProxy classProxy = new ClassProxy(TestDerived.class);
        TestDerived instance = new TestDerived();

        MethodProxy method = classProxy.getDeclared("derivedMethod");
        assertTrue(method.isPresent());
        assertEquals(instance.derivedMethod(), method.invoke(instance));

        method = classProxy.getDeclared("basePublicMethod");
        assertFalse(method.isPresent());
        assertNull(method.invoke(instance));

        method = classProxy.getDeclared("baseProtectedMethod");
        assertFalse(method.isPresent());
        assertNull(method.invoke(instance));

        ClassProxy baseProxy = new ClassProxy(TestBase.class);
        TestBase baseInstance = new TestBase();

        method = baseProxy.getDeclared("basePublicMethod");
        assertTrue(method.isPresent());
        assertEquals(baseInstance.basePublicMethod(), method.invoke(instance));

        method = baseProxy.getDeclared("baseProtectedMethod");
        assertTrue(method.isPresent());
        assertEquals(baseInstance.baseProtectedMethod(), method.invoke(instance));
    }

    @Test
    public void methodFinding_Get() {
        ClassProxy classProxy = new ClassProxy(TestDerived.class);
        TestDerived instance = new TestDerived();
        TestBase baseInstance = new TestBase();

        MethodProxy method = classProxy.getMethod("derivedMethod");
        assertTrue(method.isPresent());
        assertEquals(instance.derivedMethod(), method.invoke(instance));

        method = classProxy.getMethod("basePublicMethod");
        assertTrue(method.isPresent());
        assertEquals(baseInstance.basePublicMethod(), method.invoke(instance));

        method = classProxy.getMethod("baseProtectedMethod");
        assertFalse(method.isPresent());
        assertNull(method.invoke(instance));

        ClassProxy baseProxy = new ClassProxy(TestBase.class);

        method = baseProxy.getMethod("basePublicMethod");
        assertTrue(method.isPresent());
        assertEquals(baseInstance.basePublicMethod(), method.invoke(instance));

        method = baseProxy.getMethod("baseProtectedMethod");
        assertFalse(method.isPresent());
        assertNull(method.invoke(instance));
    }
}
