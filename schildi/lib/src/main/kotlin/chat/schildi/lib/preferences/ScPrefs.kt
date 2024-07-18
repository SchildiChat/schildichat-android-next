package chat.schildi.lib.preferences

import chat.schildi.lib.R
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.ui.strings.CommonStrings

object ScPrefs {

    object SpaceUnreadCountMode {
        const val MESSAGES = "MESSAGES"
        const val CHATS = "CHATS"
        const val HIDE = "HIDE"
    }

    // Appearance
    val SC_THEME = ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title, upstreamChoice = false)
    val SC_DYNAMICCOLORS = ScBoolPref("SC_DYNAMICCOLORS", true, R.string.sc_pref_sc_dynamic_colors_title, upstreamChoice = false)
    val EL_TYPOGRAPHY = ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary, upstreamChoice = true)

    // General behavior
    val FAST_TRANSITIONS = ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary, upstreamChoice = false)
    val NOTIFICATION_ONLY_ALERT_ONCE = ScBoolPref("NOTIFICATION_ONLY_ALERT_ONCE", false, R.string.sc_pref_notification_only_alert_once_title, R.string.sc_pref_notification_only_alert_once_summary, upstreamChoice = false)

    // Chat overview
    val COMPACT_APP_BAR = ScBoolPref("COMPACT_APP_BAR", true, R.string.sc_pref_compact_app_bar_title, R.string.sc_pref_compact_app_bar_summary, upstreamChoice = false)
    val SC_OVERVIEW_LAYOUT = ScBoolPref("SC_OVERVIEW_LAYOUT", true, R.string.sc_pref_sc_overview_layout_title, upstreamChoice = false)
    val CLIENT_GENERATED_UNREAD_COUNTS = ScBoolPref("CLIENT_GENERATED_UNREAD_COUNTS", false, R.string.sc_client_generated_unread_counts_title, R.string.sc_client_generated_unread_counts_summary, upstreamChoice = true, authorsChoice = false)
    val PIN_FAVORITES = ScBoolPref("PIN_FAVORITES", false, R.string.sc_pref_pin_favorites_title, R.string.sc_pref_pin_favorites_summary, upstreamChoice = false, authorsChoice = true)
    val BURY_LOW_PRIORITY = ScBoolPref("BURY_LOW_PRIORITY", false, R.string.sc_pref_bury_low_priority_title, R.string.sc_pref_bury_low_priority_summary, upstreamChoice = false, authorsChoice = false)
    val CLIENT_SIDE_SORT = ScBoolPref("CLIENT_SIDE_SORT", false, R.string.sc_pref_client_side_sort_title, R.string.sc_pref_client_side_sort_summary, upstreamChoice = false, authorsChoice = false)
    val SORT_BY_ACTIVITY = ScBoolPref("SORT_BY_ACTIVITY", false, R.string.sc_pref_client_side_activity_sort_title, R.string.sc_pref_client_side_activity_sort_summary, authorsChoice = false, dependencies = CLIENT_SIDE_SORT.asDependencies())
    val DUAL_MENTION_UNREAD_COUNTS = ScBoolPref("DUAL_MENTION_UNREAD_COUNTS", false, R.string.sc_pref_dual_mention_unread_counts_title, R.string.sc_pref_dual_mention_unread_counts_summary, authorsChoice = true, dependencies = SC_OVERVIEW_LAYOUT.asDependencies())
    // Spaces
    val SPACE_NAV = ScBoolPref("SPACE_NAV", false, R.string.sc_space_nav_title, R.string.sc_space_nav_summary, upstreamChoice = false, authorsChoice = true)
    val COMPACT_ROOT_SPACES = ScBoolPref("COMPACT_ROOT_SPACES", false, R.string.sc_compact_root_spaces_title, R.string.sc_compact_root_spaces_summary, authorsChoice = true, dependencies = SPACE_NAV.asDependencies())
    val SPACE_UNREAD_COUNTS = ScStringListPref(
        "SPACE_UNREAD_COUNTS",
        SpaceUnreadCountMode.MESSAGES,
        arrayOf(SpaceUnreadCountMode.MESSAGES, SpaceUnreadCountMode.CHATS, SpaceUnreadCountMode.HIDE),
        R.array.sc_space_unread_counts_names,
        null,
        R.string.sc_space_unread_counts_mode_title,
        dependencies = SPACE_NAV.asDependencies(),
    )
    val SPACE_SWIPE = ScBoolPref("SPACE_SWIPE", false, R.string.sc_space_swipe_title, R.string.sc_space_swipe_summary, upstreamChoice = false, authorsChoice = true, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_FAVORITES = ScBoolPref("PSEUDO_SPACE_FAVORITES", false, R.string.sc_pseudo_space_favorites, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_DMS = ScBoolPref("PSEUDO_SPACE_DMS", false, R.string.sc_pseudo_space_dms, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_GROUPS = ScBoolPref("PSEUDO_SPACE_GROUPS", false, R.string.sc_pseudo_space_groups, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_SPACELESS_GROUPS = ScBoolPref("PSEUDO_SPACE_SPACELESS_GROUPS", false, R.string.sc_pseudo_space_spaceless_groups, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_SPACELESS = ScBoolPref("PSEUDO_SPACE_SPACELESS", false, R.string.sc_pseudo_space_spaceless, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_NOTIFICATIONS = ScBoolPref("PSEUDO_SPACE_NOTIFICATIONS", false, R.string.sc_pseudo_space_notifications, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_UNREAD = ScBoolPref("PSEUDO_SPACE_UNREAD", false, R.string.sc_pseudo_space_unread, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_HIDE_EMPTY_UNREAD = ScBoolPref("PSEUDO_SPACE_HIDE_EMPTY_UNREAD", false, R.string.sc_pseudo_space_hide_empty_unread, null, dependencies = listOf(
        ScPrefFulfilledForAnyDependency(listOf(PSEUDO_SPACE_NOTIFICATIONS.toDependency(), PSEUDO_SPACE_UNREAD.toDependency()))
    ), authorsChoice = true)
    val ELEMENT_ROOM_LIST_FILTERS = ScBoolPref("ELEMENT_ROOM_LIST_FILTERS", false, R.string.sc_upstream_feature_flag_room_list_filters, R.string.sc_upstream_feature_flag_room_list_filters_summary, authorsChoice = false, upstreamChoice = true)

    // Timeline
    val SC_TIMELINE_LAYOUT = ScBoolPref("SC_TIMELINE_LAYOUT", true, R.string.sc_pref_sc_timeline_layout_title, upstreamChoice = false)
    val FLOATING_DATE = ScBoolPref("FLOATING_DATE", true, R.string.sc_pref_sc_floating_date_title, R.string.sc_pref_sc_floating_date_summary, upstreamChoice = false)
    val PL_DISPLAY_NAME = ScBoolPref("PL_DISPLAY_NAME", false, R.string.sc_pref_pl_display_name_title, R.string.sc_pref_pl_display_name_summary_warning, authorsChoice = false, upstreamChoice = false)
    val SYNC_READ_RECEIPT_AND_MARKER = ScBoolPref("SYNC_READ_RECEIPT_AND_MARKER", false, R.string.sc_sync_read_receipt_and_marker_title, R.string.sc_sync_read_receipt_and_marker_summary, authorsChoice = true)
    val PREFER_FREEFORM_REACTIONS = ScBoolPref("PREFER_FREEFORM_REACTIONS", false, R.string.sc_pref_prefer_freeform_reactions_title, R.string.sc_pref_prefer_freeform_reactions_summary, authorsChoice = false)
    val PREFER_FULLSCREEN_REACTION_SHEET = ScBoolPref("PREFER_FULLSCREEN_REACTION_SHEET", false, R.string.sc_pref_prefer_fullscreen_reaction_sheet_title, R.string.sc_pref_prefer_fullscreen_reaction_sheet_summary, authorsChoice = false, upstreamChoice = false)
    val JUMP_TO_UNREAD = ScBoolPref("JUMP_TO_UNREAD", false, R.string.sc_pref_jump_to_unread_title, R.string.sc_pref_jump_to_unread_option_summary, authorsChoice = true, upstreamChoice = false)

    // Developer options
    val SC_PUSH_INFO = ScActionablePref("SC_PUSH_INFO", R.string.sc_push_info_title, R.string.sc_push_info_summary)
    val SC_DEV_QUICK_OPTIONS = ScBoolPref("SC_DEV_QUICK_OPTIONS", false, R.string.sc_pref_dev_quick_options, authorsChoice = true)
    val READ_MARKER_DEBUG = ScBoolPref("READ_MARKER_DEBUG", false, R.string.sc_pref_debug_read_marker, authorsChoice = true, upstreamChoice = false)
    private val SC_DANGER_ZONE = ScBoolPref("SC_DANGER_ZONE", false, R.string.sc_pref_danger_zone, authorsChoice = true)
    val SC_RESTORE_DEFAULTS = ScActionablePref("SC_RESTORE_DEFAULTS", R.string.sc_pref_restore_defaults, dependencies = SC_DANGER_ZONE.asDependencies())
    val SC_RESTORE_UPSTREAM = ScActionablePref("SC_RESTORE_UPSTREAM", R.string.sc_pref_restore_element, dependencies = SC_DANGER_ZONE.asDependencies())
    val SC_RESTORE_AUTHORS_CHOICE = ScActionablePref("SC_RESTORE_AUTHORS_CHOICE", R.string.sc_pref_restore_authors_choice, dependencies = SC_DANGER_ZONE.asDependencies())

    // Tests to be removed before release
    /*
    val SC_TEST = ScStringListPref("TEST", "b", arrayOf("a", "b", "c"), arrayOf("A", "B", "C"), null, R.string.test)
    val SC_BUBBLE_BG_DARK_OUT = ScColorPref("SC_BUBBLE_BG_DARK_OUT", 0x008bc34a, R.string.test)
    val SC_BUBBLE_BG_LIGHT_OUT = ScColorPref("SC_BUBBLE_BG_LIGHT_OUT", 0x008bc34a, R.string.test)
     */

    val scTweaks = ScPrefScreen(R.string.sc_pref_tweaks_title, null, listOf<AbstractScPref>(
        ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            SC_DYNAMICCOLORS,
            EL_TYPOGRAPHY,
        )),
        ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
            FAST_TRANSITIONS,
        )),
        ScPrefCategory(R.string.sc_pref_category_chat_overview, null, listOf(
            SC_OVERVIEW_LAYOUT,
            COMPACT_APP_BAR,
            ELEMENT_ROOM_LIST_FILTERS,
        )),
        ScPrefCategory(R.string.sc_pref_category_spaces, null, listOf(
            SPACE_NAV,
            SPACE_UNREAD_COUNTS,
            SPACE_SWIPE,
            COMPACT_ROOT_SPACES,
            ScPrefScreen(R.string.sc_pseudo_spaces_title, R.string.sc_pseudo_spaces_summary_experimental, listOf(
                ScPrefCategory(R.string.sc_pseudo_spaces_title, null, listOf(
                    PSEUDO_SPACE_FAVORITES,
                    PSEUDO_SPACE_DMS,
                    PSEUDO_SPACE_GROUPS,
                    PSEUDO_SPACE_SPACELESS_GROUPS,
                    PSEUDO_SPACE_SPACELESS,
                    PSEUDO_SPACE_NOTIFICATIONS,
                    PSEUDO_SPACE_UNREAD,
                )),
                ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
                    PSEUDO_SPACE_HIDE_EMPTY_UNREAD,
                )),
            ), dependencies = SPACE_NAV.asDependencies())
        )),
        ScPrefCategory(R.string.sc_pref_category_timeline, null, listOf(
            SC_TIMELINE_LAYOUT,
            FLOATING_DATE,
            PREFER_FREEFORM_REACTIONS,
            PREFER_FULLSCREEN_REACTION_SHEET,
        )),
        ScPrefCategory(R.string.sc_pref_category_misc, null, listOf(
            NOTIFICATION_ONLY_ALERT_ONCE,
            ScPrefScreen(R.string.sc_pref_screen_experimental_title, R.string.sc_pref_screen_experimental_summary, listOf(
                ScPrefCategory(R.string.sc_pref_category_chat_overview, null, listOf(
                    CLIENT_GENERATED_UNREAD_COUNTS,
                    PIN_FAVORITES,
                    BURY_LOW_PRIORITY,
                    CLIENT_SIDE_SORT,
                    SORT_BY_ACTIVITY,
                    DUAL_MENTION_UNREAD_COUNTS,
                )),
                ScPrefCategory(R.string.sc_pref_category_timeline, null, listOf(
                    PL_DISPLAY_NAME,
                    JUMP_TO_UNREAD,
                    SYNC_READ_RECEIPT_AND_MARKER,
                )),
            )),
        )),
        ScPrefCategory(CommonStrings.common_developer_options, null, listOf(
            SC_PUSH_INFO,
            SC_DEV_QUICK_OPTIONS,
            READ_MARKER_DEBUG,
            SC_DANGER_ZONE,
            SC_RESTORE_DEFAULTS,
            SC_RESTORE_UPSTREAM,
            SC_RESTORE_AUTHORS_CHOICE,
        )),
        /*
        ScPrefCategory(R.string.test, null, listOf(
            ScPrefScreen(R.string.test, null, listOf(
                SC_TEST,
                SC_BUBBLE_BG_DARK_OUT,
                SC_BUBBLE_BG_LIGHT_OUT,
            )),
        )),
         */
    ))

    val devQuickTweaksOverview = listOf(
        CLIENT_GENERATED_UNREAD_COUNTS,
        ELEMENT_ROOM_LIST_FILTERS, // Used to be: ScUpstreamFeatureFlagAliasPref(FeatureFlags.RoomListFilters, R.string.sc_upstream_feature_flag_room_list_filters),
        ScPrefCategory(R.string.sc_pref_category_chat_sorting, null, listOf(
            PIN_FAVORITES,
            BURY_LOW_PRIORITY,
            CLIENT_SIDE_SORT.copy(titleRes = R.string.sc_pref_client_side_sort_title_short),
            SORT_BY_ACTIVITY,
        )),
        ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            SC_DYNAMICCOLORS,
            SC_OVERVIEW_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
            EL_TYPOGRAPHY,
            COMPACT_APP_BAR,
        )),
        ScPrefCategory(R.string.sc_pref_category_misc, null, listOf(
            DUAL_MENTION_UNREAD_COUNTS.copy(titleRes = R.string.sc_pref_dual_mention_unread_counts_title_short),
            SPACE_NAV,
            COMPACT_ROOT_SPACES,
            ScPrefCategory(R.string.sc_pseudo_spaces_title, null, listOf(
                PSEUDO_SPACE_FAVORITES,
                PSEUDO_SPACE_DMS,
                PSEUDO_SPACE_GROUPS,
                PSEUDO_SPACE_SPACELESS_GROUPS.copy(titleRes = R.string.sc_pseudo_space_spaceless_groups_short),
                PSEUDO_SPACE_SPACELESS.copy(titleRes = R.string.sc_pseudo_space_spaceless_short),
                PSEUDO_SPACE_NOTIFICATIONS.copy(titleRes = R.string.sc_pseudo_space_notifications_short),
                PSEUDO_SPACE_UNREAD,
                PSEUDO_SPACE_HIDE_EMPTY_UNREAD,
            ), dependencies = SPACE_NAV.asDependencies()),
            SYNC_READ_RECEIPT_AND_MARKER,
        )),
    )

    val devQuickTweaksTimeline = listOf(
        SC_THEME,
        SC_DYNAMICCOLORS,
        EL_TYPOGRAPHY,
        SC_TIMELINE_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
        ScPrefCategory(R.string.sc_pref_screen_experimental_title, null, listOf(
            PL_DISPLAY_NAME,
            READ_MARKER_DEBUG,
        )),
    )
}
