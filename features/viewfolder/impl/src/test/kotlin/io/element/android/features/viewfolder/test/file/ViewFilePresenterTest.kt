/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.viewfolder.test.file

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.viewfolder.impl.file.FileContentReader
import io.element.android.features.viewfolder.impl.file.FileSave
import io.element.android.features.viewfolder.impl.file.FileShare
import io.element.android.features.viewfolder.impl.file.ViewFileEvents
import io.element.android.features.viewfolder.impl.file.ViewFilePresenter
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ViewFilePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(listOf("aLine"))
        }
        val presenter = createPresenter(fileContentReader = fileContentReader)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.name).isEqualTo("aName")
            assertThat(initialState.lines.size).isEqualTo(1)
            assertThat(initialState.lines.first()).isEqualTo("aLine")
        }
    }

    @Test
    fun `present - share should not have any side effect`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(listOf("aLine"))
        }
        val fileShare = FakeFileShare()
        val fileSave = FakeFileSave()
        val presenter = createPresenter(fileContentReader = fileContentReader, fileShare = fileShare, fileSave = fileSave)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ViewFileEvents.Share)
            assertThat(fileShare.hasBeenCalled).isTrue()
            assertThat(fileSave.hasBeenCalled).isFalse()
        }
    }

    @Test
    fun `present - save should not have any side effect`() = runTest {
        val fileContentReader = FakeFileContentReader().apply {
            givenResult(listOf("aLine"))
        }
        val fileShare = FakeFileShare()
        val fileSave = FakeFileSave()
        val presenter = createPresenter(fileContentReader = fileContentReader, fileShare = fileShare, fileSave = fileSave)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(ViewFileEvents.SaveOnDisk)
            assertThat(fileShare.hasBeenCalled).isFalse()
            assertThat(fileSave.hasBeenCalled).isTrue()
        }
    }

    private fun createPresenter(
        path: String = "aPath",
        name: String = "aName",
        fileContentReader: FileContentReader = FakeFileContentReader(),
        fileShare: FileShare = FakeFileShare(),
        fileSave: FileSave = FakeFileSave(),
    ) = ViewFilePresenter(
        path = path,
        name = name,
        fileContentReader = fileContentReader,
        fileShare = fileShare,
        fileSave = fileSave,
    )
}
