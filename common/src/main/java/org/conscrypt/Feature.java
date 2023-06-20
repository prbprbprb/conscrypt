/*
 * Copyright 2023 The Android Open Source Project
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

enum Feature {
    EXTRA_DEBUG(false),
    TLS_V11(true);

    private final boolean enabledByDefault;
    private final FeatureHandler handler;

    Feature(boolean enabledByDefault) {
        this(enabledByDefault, new FeatureHandler());
    }

    Feature(boolean enabledByDefault, FeatureHandler handler) {
        this.enabledByDefault = enabledByDefault;
        this.handler = handler;
    }

    boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public FeatureHandler getHandler() {
        return handler;
    }
}
