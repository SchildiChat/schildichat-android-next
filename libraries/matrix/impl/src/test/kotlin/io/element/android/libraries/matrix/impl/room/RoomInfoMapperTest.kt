/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomHero
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomInfo
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomMember
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomPowerLevels
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_6
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevelValues
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test
import org.matrix.rustcomponents.sdk.Membership
import uniffi.matrix_sdk_base.EncryptionState
import org.matrix.rustcomponents.sdk.JoinRule as RustJoinRule
import org.matrix.rustcomponents.sdk.RoomHistoryVisibility as RustRoomHistoryVisibility
import org.matrix.rustcomponents.sdk.RoomNotificationMode as RustRoomNotificationMode

class RoomInfoMapperTest {
    @Test
    fun `mapping of RustRoomInfo should map all the fields`() {
        assertThat(
            RoomInfoMapper().map(
                aRustRoomInfo(
                    id = A_ROOM_ID.value,
                    displayName = "displayName",
                    rawName = "rawName",
                    topic = "topic",
                    avatarUrl = AN_AVATAR_URL,
                    encryptionState = EncryptionState.ENCRYPTED,
                    isDirect = true,
                    isPublic = false,
                    isSpace = false,
                    joinRule = RustJoinRule.Invite,
                    successorRoom = null,
                    isFavourite = false,
                    canonicalAlias = A_ROOM_ALIAS.value,
                    alternativeAliases = listOf(A_ROOM_ALIAS.value),
                    membership = Membership.JOINED,
                    inviter = aRustRoomMember(A_USER_ID),
                    heroes = listOf(aRustRoomHero()),
                    activeMembersCount = 2uL,
                    invitedMembersCount = 3uL,
                    joinedMembersCount = 4uL,
                    roomPowerLevels = FakeFfiRoomPowerLevels(users = mapOf(A_USER_ID_6.value to 50L)),
                    highlightCount = 10uL,
                    notificationCount = 11uL,
                    userDefinedNotificationMode = RustRoomNotificationMode.MUTE,
                    hasRoomCall = true,
                    activeRoomCallParticipants = listOf(A_USER_ID_3.value),
                    isMarkedUnread = false,
                    numUnreadMessages = 12uL,
                    numUnreadNotifications = 13uL,
                    numUnreadMentions = 14uL,
                    pinnedEventIds = listOf(AN_EVENT_ID.value),
                    roomCreators = listOf(A_USER_ID.value),
                    historyVisibility = RustRoomHistoryVisibility.Joined,
                    roomVersion = "12",
                    privilegedCreatorsRole = true,
                )
            )
        ).isEqualTo(
            RoomInfo(
                id = A_ROOM_ID,
                name = "displayName",
                rawName = "rawName",
                topic = "topic",
                avatarUrl = AN_AVATAR_URL,
                isPublic = false,
                isDirect = true,
                isEncrypted = true,
                isSpace = false,
                isFavorite = false,
                joinRule = JoinRule.Invite,
                canonicalAlias = A_ROOM_ALIAS,
                alternativeAliases = listOf(A_ROOM_ALIAS).toImmutableList(),
                currentUserMembership = CurrentUserMembership.JOINED,
                inviter = aRoomMember(A_USER_ID),
                activeMembersCount = 2L,
                invitedMembersCount = 3L,
                joinedMembersCount = 4L,
                roomPowerLevels = RoomPowerLevels(
                    values = defaultRoomPowerLevelValues(),
                    users = persistentMapOf(A_USER_ID_6 to 50L)
                ),
                highlightCount = 10L,
                notificationCount = 11L,
                userDefinedNotificationMode = RoomNotificationMode.MUTE,
                hasRoomCall = true,
                activeRoomCallParticipants = listOf(A_USER_ID_3).toImmutableList(),
                heroes = listOf(
                    MatrixUser(
                        userId = A_USER_ID,
                        displayName = "displayName",
                        avatarUrl = "avatarUrl",
                    )
                ).toImmutableList(),
                pinnedEventIds = listOf(AN_EVENT_ID).toPersistentList(),
                creators = persistentListOf(A_USER_ID),
                isMarkedUnread = false,
                numUnreadMessages = 12L,
                numUnreadNotifications = 13L,
                numUnreadMentions = 14L,
                historyVisibility = RoomHistoryVisibility.Joined,
                successorRoom = null,
                roomVersion = "12",
                privilegedCreatorRole = true,
            )
        )
    }

    @Test
    fun `mapping of RustRoomInfo with null members should map all the fields`() {
        assertThat(
            RoomInfoMapper().map(
                aRustRoomInfo(
                    id = A_ROOM_ID.value,
                    displayName = null,
                    rawName = null,
                    topic = null,
                    avatarUrl = null,
                    encryptionState = EncryptionState.UNKNOWN,
                    isDirect = false,
                    isPublic = true,
                    joinRule = null,
                    isSpace = false,
                    successorRoom = null,
                    isFavourite = true,
                    canonicalAlias = null,
                    alternativeAliases = emptyList(),
                    membership = Membership.INVITED,
                    inviter = null,
                    heroes = listOf(aRustRoomHero()),
                    activeMembersCount = 2uL,
                    invitedMembersCount = 3uL,
                    joinedMembersCount = 4uL,
                    roomPowerLevels = FakeFfiRoomPowerLevels(),
                    highlightCount = 10uL,
                    notificationCount = 11uL,
                    userDefinedNotificationMode = null,
                    hasRoomCall = false,
                    activeRoomCallParticipants = emptyList(),
                    isMarkedUnread = true,
                    numUnreadMessages = 12uL,
                    numUnreadNotifications = 13uL,
                    numUnreadMentions = 14uL,
                    pinnedEventIds = emptyList(),
                    roomCreators = null,
                    roomVersion = "12",
                    privilegedCreatorsRole = true,
                )
            )
        ).isEqualTo(
            RoomInfo(
                id = A_ROOM_ID,
                name = null,
                rawName = null,
                topic = null,
                avatarUrl = null,
                isEncrypted = null,
                isPublic = true,
                isDirect = false,
                joinRule = null,
                isSpace = false,
                successorRoom = null,
                isFavorite = true,
                canonicalAlias = null,
                alternativeAliases = emptyList<RoomAlias>().toPersistentList(),
                currentUserMembership = CurrentUserMembership.INVITED,
                inviter = null,
                activeMembersCount = 2L,
                invitedMembersCount = 3L,
                joinedMembersCount = 4L,
                roomPowerLevels = RoomPowerLevels(
                    values = defaultRoomPowerLevelValues(),
                    users = persistentMapOf(),
                ),
                highlightCount = 10L,
                notificationCount = 11L,
                userDefinedNotificationMode = null,
                hasRoomCall = false,
                activeRoomCallParticipants = emptyList<UserId>().toImmutableList(),
                heroes = emptyList<MatrixUser>().toImmutableList(),
                pinnedEventIds = emptyList<EventId>().toPersistentList(),
                creators = persistentListOf(),
                isMarkedUnread = true,
                numUnreadMessages = 12L,
                numUnreadNotifications = 13L,
                numUnreadMentions = 14L,
                historyVisibility = RoomHistoryVisibility.Joined,
                roomVersion = "12",
                privilegedCreatorRole = true,
            )
        )
    }

    @Test
    fun `mapping Membership`() {
        assertThat(Membership.INVITED.map()).isEqualTo(CurrentUserMembership.INVITED)
        assertThat(Membership.JOINED.map()).isEqualTo(CurrentUserMembership.JOINED)
        assertThat(Membership.LEFT.map()).isEqualTo(CurrentUserMembership.LEFT)
    }

    @Test
    fun `mapping RoomNotificationMode`() {
        assertThat(RustRoomNotificationMode.ALL_MESSAGES.map()).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        assertThat(RustRoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY.map()).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        assertThat(RustRoomNotificationMode.MUTE.map()).isEqualTo(RoomNotificationMode.MUTE)
    }
}
