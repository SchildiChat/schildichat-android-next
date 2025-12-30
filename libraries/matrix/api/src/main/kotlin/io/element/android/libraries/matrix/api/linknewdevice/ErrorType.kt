/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

sealed class ErrorType(message: String) : Exception(message) {
    /**
     * The requested device ID is already in use.
     */
    class DeviceIdAlreadyInUse(message: String) : ErrorType(message)

    /**
     * The check code was incorrect.
     */
    class InvalidCheckCode(message: String) : ErrorType(message)

    /**
     * The other client proposed an unsupported protocol.
     */
    class UnsupportedProtocol(message: String) : ErrorType(message)

    /**
     * Secrets backup not set up properly.
     */
    class MissingSecretsBackup(message: String) : ErrorType(message)

    /**
     * The rendezvous session was not found and might have expired.
     */
    class NotFound(message: String) : ErrorType(message)

    /**
     * The device could not be created.
     */
    class UnableToCreateDevice(message: String) : ErrorType(message)

    /**
     * An unknown error has happened.
     */
    class Unknown(message: String) : ErrorType(message)
}
