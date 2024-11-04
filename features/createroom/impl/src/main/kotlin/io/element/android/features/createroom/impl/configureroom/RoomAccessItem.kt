/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.annotation.StringRes
import io.element.android.libraries.ui.strings.CommonStrings

enum class RoomAccessItem(
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    Anyone(
        title = CommonStrings.screen_create_room_access_section_anyone_option_title,
        description = CommonStrings.screen_create_room_access_section_anyone_option_description,
    ),
    AskToJoin(
        title = CommonStrings.screen_create_room_access_section_knocking_option_title,
        description = CommonStrings.screen_create_room_access_section_knocking_option_description,
    ),
}
