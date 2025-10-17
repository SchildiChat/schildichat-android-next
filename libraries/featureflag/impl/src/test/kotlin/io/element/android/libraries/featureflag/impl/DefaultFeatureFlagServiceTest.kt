/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeature
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFeatureFlagServiceTest {
    @Test
    fun `given service without provider when feature is checked then it returns the default value`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagService = createDefaultFeatureFlagService(buildMeta = buildMeta)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.Space).test {
            assertThat(awaitItem()).isEqualTo(FeatureFlags.Space.defaultValue(buildMeta))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given service without provider when set enabled feature is called then it returns false`() = runTest {
        val featureFlagService = createDefaultFeatureFlagService()
        val result = featureFlagService.setFeatureEnabled(FeatureFlags.Space, true)
        assertThat(result).isFalse()
    }

    @Test
    fun `given service with a runtime provider when set enabled feature is called then it returns true`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(featureFlagProvider),
            buildMeta = buildMeta,
        )
        val result = featureFlagService.setFeatureEnabled(FeatureFlags.Space, true)
        assertThat(result).isTrue()
    }

    @Test
    fun `given service with a runtime provider and feature enabled when feature is checked then it returns the correct value`() = runTest {
        val buildMeta = aBuildMeta()
        val featureFlagProvider = FakeMutableFeatureFlagProvider(0, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(featureFlagProvider),
            buildMeta = buildMeta
        )
        featureFlagService.setFeatureEnabled(FeatureFlags.Space, true)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.Space).test {
            assertThat(awaitItem()).isTrue()
            featureFlagService.setFeatureEnabled(FeatureFlags.Space, false)
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `given service with 2 runtime providers when feature is checked then it uses the priority correctly`() = runTest {
        val buildMeta = aBuildMeta()
        val lowPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(LOW_PRIORITY, buildMeta)
        val highPriorityFeatureFlagProvider = FakeMutableFeatureFlagProvider(HIGH_PRIORITY, buildMeta)
        val featureFlagService = createDefaultFeatureFlagService(
            providers = setOf(lowPriorityFeatureFlagProvider, highPriorityFeatureFlagProvider),
            buildMeta = buildMeta
        )
        lowPriorityFeatureFlagProvider.setFeatureEnabled(FeatureFlags.Space, false)
        highPriorityFeatureFlagProvider.setFeatureEnabled(FeatureFlags.Space, true)
        featureFlagService.isFeatureEnabledFlow(FeatureFlags.Space).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `getAvailableFeatures should return expected features`() {
        val aFinishedLabFeature = FakeFeature(
            key = "finished_lab_feature",
            title = "Finished Lab Feature",
            isFinished = true,
            isInLabs = true,
        )
        val aFinishedDevFeature = FakeFeature(
            key = "finished_dev_feature",
            title = "Finished Dev Feature",
            isFinished = true,
            isInLabs = false,
        )
        val anUnfinishedLabFeature = FakeFeature(
            key = "unfinished_lab_feature",
            title = "Unfinished Lab Feature",
            isFinished = false,
            isInLabs = true,
        )
        val anUnfinishedDevFeature = FakeFeature(
            key = "unfinished_dev_feature",
            title = "Unfinished Dev Feature",
            isFinished = false,
            isInLabs = false,
        )
        val featureFlagService = createDefaultFeatureFlagService(
            features = listOf(
                aFinishedLabFeature,
                aFinishedDevFeature,
                anUnfinishedLabFeature,
                anUnfinishedDevFeature,
            ),
        )
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishFeatures = false,
                isInLabs = true,
            )
        ).containsExactly(anUnfinishedLabFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishFeatures = true,
                isInLabs = true,
            )
        ).containsExactly(aFinishedLabFeature, anUnfinishedLabFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishFeatures = false,
                isInLabs = false,
            )
        ).containsExactly(anUnfinishedDevFeature)
        assertThat(
            featureFlagService.getAvailableFeatures(
                includeFinishFeatures = true,
                isInLabs = false,
            )
        ).containsExactly(aFinishedDevFeature, anUnfinishedDevFeature)
    }
}

private fun createDefaultFeatureFlagService(
    providers: Set<FeatureFlagProvider> = emptySet(),
    buildMeta: BuildMeta = aBuildMeta(),
    features: List<Feature> = FeatureFlags.entries,
) = DefaultFeatureFlagService(
    providers = providers,
    buildMeta = buildMeta,
    featuresProvider = { features }
)
