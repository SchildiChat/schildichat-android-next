/*
 * Copyright 2022 The Android Open Source Project
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ui

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screenshot test for the English version only.
 */
@RunWith(TestParameterInjector::class)
class S : ScreenshotTest() {
    /**
     * *Note*: keep the method name as short as possible to get shorter filename for generated screenshot.
     * Long name was preview_test.
     */
    @Test
    fun t(
        @TestParameter(valuesProvider = PreviewProvider::class) componentTestPreview: TestPreview,
        @TestParameter baseDeviceConfig: BaseDeviceConfig,
        @TestParameter(value = ["1.0"]) fontScale: Float,
        // Need to keep the TestParameter to have filename including the language.
        @TestParameter(value = ["en"]) localeStr: String,
    ) {
        doTest(
            componentTestPreview = componentTestPreview,
            baseDeviceConfig = baseDeviceConfig,
            fontScale = fontScale,
            localeStr = localeStr,
        )
    }
}
