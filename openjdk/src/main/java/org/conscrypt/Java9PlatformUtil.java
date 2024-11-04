/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conscrypt;

import javax.net.ssl.SSLParameters;

/**
 * Utility methods supported on Java 9+.
 */
final class Java9PlatformUtil {
    static final class SSLParametersProxy extends ClassProxy {
        private static class Holder {
            private  static final SSLParametersProxy INSTANCE = new SSLParametersProxy();
        }
        private final MethodProxy getApplicationProtocols;
        private final MethodProxy setApplicationProtocols;

        SSLParametersProxy() {
            super(SSLParameters.class);
            getApplicationProtocols = getMethod("getApplicationProtocols")
                    .setReturnType(String[].class)
                    .setDefaultValue(EmptyArray.STRING);
            setApplicationProtocols =
                    getMethod("setApplicationProtocols", String[].class);
        }

        public String[] getApplicationProtocols(SSLParameters params) {
            return (String[]) getApplicationProtocols.invoke(params);
        }

        public void setApplicationProtocols(SSLParameters params, String[] protocols) {
            setApplicationProtocols.invoke(params, (Object) protocols);
        }

        static SSLParametersProxy get() {
            return Holder.INSTANCE;
        }
    }

    static void setSSLParameters(
            SSLParameters src, SSLParametersImpl dest, AbstractConscryptSocket socket) {
        Java8PlatformUtil.setSSLParameters(src, dest, socket);

        dest.setApplicationProtocols(SSLParametersProxy.get().getApplicationProtocols(src));
    }

    static void getSSLParameters(
            SSLParameters dest, SSLParametersImpl src, AbstractConscryptSocket socket) {
        Java8PlatformUtil.getSSLParameters(dest, src, socket);

        SSLParametersProxy.get().setApplicationProtocols(dest, src.getApplicationProtocols());
    }

    static void setSSLParameters(
            SSLParameters src, SSLParametersImpl dest, ConscryptEngine engine) {
        Java8PlatformUtil.setSSLParameters(src, dest, engine);

        dest.setApplicationProtocols(SSLParametersProxy.get().getApplicationProtocols(src));
    }

    static void getSSLParameters(
            SSLParameters dest, SSLParametersImpl src, ConscryptEngine engine) {
        Java8PlatformUtil.getSSLParameters(dest, src, engine);

        SSLParametersProxy.get().setApplicationProtocols(dest, src.getApplicationProtocols());
    }

    private Java9PlatformUtil() {}
}
