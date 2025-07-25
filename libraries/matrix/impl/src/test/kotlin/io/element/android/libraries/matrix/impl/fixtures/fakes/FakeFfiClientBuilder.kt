/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientBuilder
import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RequestConfig
import org.matrix.rustcomponents.sdk.SlidingSyncVersionBuilder
import uniffi.matrix_sdk.BackupDownloadStrategy
import uniffi.matrix_sdk_crypto.CollectStrategy
import uniffi.matrix_sdk_crypto.DecryptionSettings

class FakeFfiClientBuilder : ClientBuilder(NoPointer) {
    override fun addRootCertificates(certificates: List<ByteArray>) = this
    override fun autoEnableBackups(autoEnableBackups: Boolean) = this
    override fun autoEnableCrossSigning(autoEnableCrossSigning: Boolean) = this
    override fun backupDownloadStrategy(backupDownloadStrategy: BackupDownloadStrategy) = this
    override fun disableAutomaticTokenRefresh() = this
    override fun disableBuiltInRootCertificates() = this
    override fun decryptionSettings(decryptionSettings: DecryptionSettings): ClientBuilder = this
    override fun disableSslVerification() = this
    override fun homeserverUrl(url: String) = this
    override fun sessionPassphrase(passphrase: String?) = this
    override fun proxy(url: String) = this
    override fun requestConfig(config: RequestConfig) = this
    override fun roomKeyRecipientStrategy(strategy: CollectStrategy) = this
    override fun serverName(serverName: String) = this
    override fun serverNameOrHomeserverUrl(serverNameOrUrl: String) = this
    override fun sessionPaths(dataPath: String, cachePath: String) = this
    override fun setSessionDelegate(sessionDelegate: ClientSessionDelegate) = this
    override fun slidingSyncVersionBuilder(versionBuilder: SlidingSyncVersionBuilder) = this
    override fun userAgent(userAgent: String) = this
    override fun username(username: String) = this
    override fun enableShareHistoryOnInvite(enableShareHistoryOnInvite: Boolean): ClientBuilder = this

    override suspend fun build(): Client {
        return FakeFfiClient(withUtdHook = {})
    }
}
