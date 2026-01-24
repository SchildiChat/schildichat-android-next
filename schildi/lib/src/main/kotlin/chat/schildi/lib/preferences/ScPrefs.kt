package chat.schildi.lib.preferences

import chat.schildi.lib.R
import io.element.android.libraries.ui.strings.CommonStrings

object ScPrefs {

    object SpaceUnreadCountMode {
        const val MESSAGES = "MESSAGES"
        const val CHATS = "CHATS"
        const val HIDE = "HIDE"
    }

    // Developer options
    private const val SC_DEVELOPER_OPTIONS_CATEGORY_KEY = "SC_DEVELOPER_OPTIONS_CATEGORY"
    private val SC_DANGER_ZONE = ScBoolPref("SC_DANGER_ZONE", false, R.string.sc_pref_danger_zone, R.string.sc_pref_danger_zone_summary, authorsChoice = true)
    val SC_PUSH_INFO = ScActionablePref("SC_PUSH_INFO", R.string.sc_push_info_title, R.string.sc_push_info_summary)
    val SC_USER_CHANGED_SETTINGS = ScActionablePref("SC_USER_CHANGED_SETTINGS", R.string.sc_pref_user_changed_prefs_title, R.string.sc_pref_user_changed_prefs_summary)
    val SC_DEV_QUICK_OPTIONS = ScBoolPref("SC_DEV_QUICK_OPTIONS", false, R.string.sc_pref_dev_quick_options, authorsChoice = true)
    val READ_MARKER_DEBUG = ScBoolPref("READ_MARKER_DEBUG", false, R.string.sc_pref_debug_read_marker, authorsChoice = true, upstreamChoice = false)
    val SC_RESTORE_DEFAULTS = ScActionablePref("SC_RESTORE_DEFAULTS", R.string.sc_pref_restore_defaults, dependencies = SC_DANGER_ZONE.asDependencies())
    val SC_RESTORE_ADVANCED_THEME_DEFAULTS = ScActionablePref("SC_RESTORE_ADVANCED_THEME_DEFAULTS", R.string.sc_pref_restore_defaults)
    val SC_RESTORE_UPSTREAM = ScActionablePref("SC_RESTORE_UPSTREAM", R.string.sc_pref_restore_element, dependencies = SC_DANGER_ZONE.asDependencies())
    val SC_RESTORE_AUTHORS_CHOICE = ScActionablePref("SC_RESTORE_AUTHORS_CHOICE", R.string.sc_pref_restore_authors_choice, dependencies = SC_DANGER_ZONE.asDependencies())

    // Appearance
    val SC_THEME = ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title, R.string.sc_pref_sc_themes_summary, upstreamChoice = false)
    val EL_TYPOGRAPHY = ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary, upstreamChoice = true)
    val TWO_INITIALS_AVATAR_FALLBACK = ScBoolPref("TWO_INITIALS_AVATAR_FALLBACK", false, R.string.sc_pref_two_initials_avatar_fallback_title, R.string.sc_pref_two_initials_avatar_fallback_summary, upstreamChoice = false, authorsChoice = true)

    // General behavior
    val FAST_TRANSITIONS = ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary, upstreamChoice = false)
    val NOTIFICATION_ONLY_ALERT_ONCE = ScBoolPref("NOTIFICATION_ONLY_ALERT_ONCE", false, R.string.sc_pref_notification_only_alert_once_title, R.string.sc_pref_notification_only_alert_once_summary, upstreamChoice = false)
    val SHOW_SYNCING_INDICATOR = ScBoolPref("SHOW_SYNCING_INDICATOR", true, R.string.sc_pref_syncing_indicator_title, R.string.sc_pref_syncing_indicator_summary, upstreamChoice = true, authorsChoice = true)
    val DEBOUNCE_OFFLINE_STATE = ScBoolPref("DEBOUNCE_OFFLINE_STATE", true, R.string.sc_pref_debounce_offline_state_title, R.string.sc_pref_debounce_offline_state_summary, authorsChoice = null, upstreamChoice = false)

    // Chat overview
    val SC_OVERVIEW_LAYOUT = ScBoolPref("SC_OVERVIEW_LAYOUT", true, R.string.sc_pref_sc_overview_layout_title, R.string.sc_pref_sc_layout_summary, upstreamChoice = false)
    val CLIENT_GENERATED_UNREAD_COUNTS = ScBoolPref("CLIENT_GENERATED_UNREAD_COUNTS", true, R.string.sc_client_generated_unread_counts_title, R.string.sc_client_generated_unread_counts_summary, upstreamChoice = true, authorsChoice = true, disabledValue = true, dependencies = SC_DANGER_ZONE.asDependencies())
    val RENDER_SILENT_UNREAD = ScBoolPref("RENDER_SILENT_UNREAD", true, R.string.sc_pref_render_silent_unread_title, R.string.sc_pref_render_silent_unread_summary, upstreamChoice = true, authorsChoice = true, disabledValue = false)
    val PIN_FAVORITES = ScBoolPref("PIN_FAVORITES", false, R.string.sc_pref_pin_favorites_title, R.string.sc_pref_pin_favorites_summary, upstreamChoice = false, authorsChoice = false)
    val BURY_LOW_PRIORITY = ScBoolPref("BURY_LOW_PRIORITY", false, R.string.sc_pref_bury_low_priority_title, R.string.sc_pref_bury_low_priority_summary, upstreamChoice = false, authorsChoice = false)
    val SORT_BY_UNREAD = ScBoolPref("SORT_BY_UNREAD", false, R.string.sc_pref_client_side_sort_by_unread_title, R.string.sc_pref_client_side_sort_by_unread_summary, upstreamChoice = false, authorsChoice = true)
    val SORT_WITH_SILENT_UNREAD = ScBoolPref(
        "SORT_WITH_SILENT_UNREAD",
        true,
        R.string.sc_pref_sort_with_silent_unread_title,
        R.string.sc_pref_sort_with_silent_unread_summary,
        upstreamChoice = false,
        authorsChoice = true,
        disabledValue = false,
        dependencies = listOf(
            SORT_BY_UNREAD.toDependency(),
            RENDER_SILENT_UNREAD.toDependency(),
        )
    )
    val DUAL_MENTION_UNREAD_COUNTS = ScBoolPref("DUAL_MENTION_UNREAD_COUNTS", false, R.string.sc_pref_dual_mention_unread_counts_title, R.string.sc_pref_dual_mention_unread_counts_summary, authorsChoice = true, dependencies = SC_OVERVIEW_LAYOUT.asDependencies())
    val HIDE_INVITES = ScBoolPref("HIDE_INVITES", false, R.string.sc_pref_hide_invites_title, R.string.sc_pref_hide_invites_summary)

    // Spaces
    val SPACE_NAV = ScBoolPref("SPACE_NAV", true, R.string.sc_space_nav_title, R.string.sc_space_nav_summary, upstreamChoice = false, authorsChoice = true)
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
    val SPACE_SWIPE = ScBoolPref("SPACE_SWIPE", true, R.string.sc_space_swipe_title, R.string.sc_space_swipe_summary, upstreamChoice = false, authorsChoice = true, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_ALL_ROOMS = ScBoolPref("PSEUDO_SPACE_ALL_CHATS", true, R.string.sc_space_all_rooms_title, R.string.sc_space_all_rooms_summary, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_FAVORITES = ScBoolPref("PSEUDO_SPACE_FAVORITES", false, R.string.sc_pseudo_space_favorites, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_DMS = ScBoolPref("PSEUDO_SPACE_DMS", false, R.string.sc_pseudo_space_dms, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_GROUPS = ScBoolPref("PSEUDO_SPACE_GROUPS", false, R.string.sc_pseudo_space_groups, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_SPACELESS_GROUPS = ScBoolPref("PSEUDO_SPACE_SPACELESS_GROUPS", false, R.string.sc_pseudo_space_spaceless_groups, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_SPACELESS = ScBoolPref("PSEUDO_SPACE_SPACELESS", false, R.string.sc_pseudo_space_spaceless, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_NOTIFICATIONS = ScBoolPref("PSEUDO_SPACE_NOTIFICATIONS", false, R.string.sc_pseudo_space_notifications, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_UNREAD = ScBoolPref("PSEUDO_SPACE_UNREAD", false, R.string.sc_pseudo_space_unread, null, dependencies = SPACE_NAV.asDependencies())
    val PSEUDO_SPACE_INVITES = ScBoolPref("PSEUDO_SPACE_INVITES", false, R.string.sc_pseudo_space_invites, null, dependencies = listOf(SPACE_NAV.toDependency(), HIDE_INVITES.toDependency().not()), authorsChoice = true)
    val PSEUDO_SPACE_HIDE_EMPTY_UNREAD = ScBoolPref("PSEUDO_SPACE_HIDE_EMPTY_UNREAD", false, R.string.sc_pseudo_space_hide_empty_unread, null, dependencies = listOf(
        ScPrefFulfilledForAnyDependency(listOf(PSEUDO_SPACE_NOTIFICATIONS.toDependency(), PSEUDO_SPACE_UNREAD.toDependency(), PSEUDO_SPACE_INVITES.toDependency()))
    ), authorsChoice = true)
    val ELEMENT_ROOM_LIST_FILTERS = ScBoolPref("ELEMENT_ROOM_LIST_FILTERS", false, R.string.sc_upstream_feature_flag_room_list_filters, R.string.sc_upstream_feature_flag_room_list_filters_summary, authorsChoice = false, upstreamChoice = true)
    // Chat overview settings depending on spaces
    val SNC_FAB = ScBoolPref("SNC_FAB", true, R.string.sc_pref_snc_fab_title, R.string.sc_pref_snc_fab_summary, disabledValue = false, authorsChoice = false, upstreamChoice = true)

    // Timeline
    val PINNED_MESSAGE_OVERLAY = ScBoolPref("PINNED_MESSAGE_OVERLAY", false, R.string.sc_pref_pinned_message_overlay_title, R.string.sc_pref_pinned_message_overlay_summary, authorsChoice = false, upstreamChoice = true)
    val PINNED_MESSAGE_TOOLBAR_ACTION = ScBoolPref("PINNED_MESSAGE_TOOLBAR_ACTION", true, R.string.sc_pref_pinned_message_toolbar_title, R.string.sc_pref_pinned_message_toolbar_summary, authorsChoice = true, upstreamChoice = false, dependencies = PINNED_MESSAGE_OVERLAY.asDependencies(expect = false), disabledValue = false)
    val HIDE_CALL_TOOLBAR_ACTION = ScBoolPref("HIDE_CALL_TOOLBAR_ACTION", false, R.string.sc_pref_hide_call_toolbar_action_title, R.string.sc_pref_hide_call_toolbar_action_summary, authorsChoice = true, upstreamChoice = false)
    val SC_TIMELINE_LAYOUT = ScBoolPref("SC_TIMELINE_LAYOUT", true, R.string.sc_pref_sc_timeline_layout_title, R.string.sc_pref_sc_layout_summary, upstreamChoice = false)
    val FLOATING_DATE = ScBoolPref("FLOATING_DATE", true, R.string.sc_pref_sc_floating_date_title, R.string.sc_pref_sc_floating_date_summary, upstreamChoice = false)
    val PL_DISPLAY_NAME = ScBoolPref("PL_DISPLAY_NAME", false, R.string.sc_pref_pl_display_name_title, R.string.sc_pref_pl_display_name_summary_warning, authorsChoice = false, upstreamChoice = false)
    val SYNC_READ_RECEIPT_AND_MARKER = ScBoolPref("SYNC_READ_RECEIPT_AND_MARKER", false, R.string.sc_sync_read_receipt_and_marker_title, R.string.sc_sync_read_receipt_and_marker_summary, authorsChoice = true, dependencies = SC_DANGER_ZONE.asDependencies())
    val MARK_READ_REQUIRES_SEEN_UNREAD_LINE = ScBoolPref("MARK_READ_REQUIRES_SEEN_UNREAD_LINE", true, R.string.sc_pref_mark_read_requires_seen_unread_line_title, R.string.sc_pref_mark_read_requires_seen_unread_line_summary, authorsChoice = false, dependencies = SYNC_READ_RECEIPT_AND_MARKER.asDependencies())
    val PREFER_FREEFORM_REACTIONS = ScBoolPref("PREFER_FREEFORM_REACTIONS", false, R.string.sc_pref_prefer_freeform_reactions_title, R.string.sc_pref_prefer_freeform_reactions_summary, authorsChoice = false)
    val PREFER_FULLSCREEN_REACTION_SHEET = ScBoolPref("PREFER_FULLSCREEN_REACTION_SHEET", false, R.string.sc_pref_prefer_fullscreen_reaction_sheet_title, R.string.sc_pref_prefer_fullscreen_reaction_sheet_summary, authorsChoice = false, upstreamChoice = false)
    val ALWAYS_SHOW_REACTION_SEARCH_BAR = ScBoolPref("ALWAYS_SHOW_REACTION_SEARCH_BAR", false, R.string.sc_pref_always_show_reaction_search_bar_title, R.string.sc_pref_always_show_reaction_search_bar_summary, authorsChoice = false, upstreamChoice = true)
    val JUMP_TO_UNREAD = ScBoolPref("JUMP_TO_UNREAD", false, R.string.sc_pref_jump_to_unread_title, R.string.sc_pref_jump_to_unread_option_summary, authorsChoice = true, upstreamChoice = false)
    //val RENDER_INLINE_IMAGES = ScBoolPref("RENDER_INLINE_IMAGES", true, R.string.sc_pref_render_inline_images_title, R.string.sc_pref_render_inline_images_summary, authorsChoice = true, upstreamChoice = false)
    val URL_PREVIEWS = ScBoolPref("URL_PREVIEWS", false, R.string.sc_url_previews_title, R.string.sc_url_previews_summary, authorsChoice = true, upstreamChoice = false)
    val URL_PREVIEWS_IN_E2EE_ROOMS = ScBoolPref("URL_PREVIEWS_IN_E2EE_ROOMS", false, R.string.sc_url_previews_in_e2ee_rooms_title, R.string.sc_url_previews_in_e2ee_rooms_summary, authorsChoice = true, upstreamChoice = false, dependencies = URL_PREVIEWS.asDependencies(), disabledValue = false)
    val URL_PREVIEWS_REQUIRE_EXPLICIT_LINKS = ScBoolPref("URL_PREVIEWS_REQUIRE_EXPLICIT_LINKS", true, R.string.sc_url_previews_require_explicit_links_title, R.string.sc_url_previews_require_explicit_links_summary, authorsChoice = true, dependencies = URL_PREVIEWS.asDependencies(), disabledValue = null)
    val REPLY_PREVIEW_LINE_COUNT = ScIntPref("REPLY_PREVIEW_LINE_COUNT", 4, R.string.sc_reply_preview_line_count_title, R.string.sc_reply_preview_line_count_summary, minValue = 1, authorsChoice = 4, upstreamChoice = 2)
    val FULLY_EXPAND_MESSAGE_MENU = ScBoolPref("FULLY_EXPAND_MESSAGE_MENU", false, R.string.sc_pref_fully_expand_message_menu_title, R.string.sc_pref_fully_expand_message_menu_summary, authorsChoice = true, upstreamChoice = false)
    val MESSAGE_CONTEXT_MENU_TEXT_SELECTABLE = ScBoolPref("MESSAGE_CONTEXT_MENU_TEXT_SELECTABLE", true, R.string.sc_pref_message_context_menu_text_selectable_title, R.string.sc_pref_message_context_menu_text_selectable_summary, authorsChoice = true, upstreamChoice = false)
    val VIEW_HIDDEN_EVENTS = ScBoolPref("VIEW_HIDDEN_EVENTS", false, R.string.sc_pref_view_hidden_events_title, authorsChoice = true, upstreamChoice = false)
    val VIEW_REDACTIONS = ScBoolPref("VIEW_REDACTIONS", true, R.string.sc_pref_view_redactions_title, R.string.sc_pref_view_redactions_summary, /*dependencies = listOf(VIEW_HIDDEN_EVENTS.toDependency(expect = false)),*/ disabledValue = true, upstreamChoice = true)
    val LEGACY_MESSAGE_RENDERING = ScBoolPref("LEGACY_MESSAGE_RENDERING", false, R.string.sc_pref_legacy_message_rendering_title, R.string.sc_pref_legacy_message_rendering_summary, authorsChoice = false, upstreamChoice = true)

    // Advanced theming options - Light theme
    val BUBBLE_BG_LIGHT_OUTGOING = ScColorPref("BUBBLE_BG_LIGHT_OUTGOING", R.string.sc_pref_bubble_color_outgoing_title)
    val BUBBLE_BG_LIGHT_INCOMING = ScColorPref("BUBBLE_BG_LIGHT_INCOMING", R.string.sc_pref_bubble_color_incoming_title)
    // Advanced theming options - Dark theme
    val BUBBLE_BG_DARK_OUTGOING = ScColorPref("BUBBLE_BG_DARK_OUTGOING", R.string.sc_pref_bubble_color_outgoing_title)
    val BUBBLE_BG_DARK_INCOMING = ScColorPref("BUBBLE_BG_DARK_INCOMING", R.string.sc_pref_bubble_color_incoming_title)

    // Tests to be removed before release
    /*
    val SC_TEST = ScStringListPref("TEST", "b", arrayOf("a", "b", "c"), arrayOf("A", "B", "C"), null, R.string.test)
     */

    // Separate collection so we can restore defaults for these only
    val scTweaksAdvancedTheming = ScPrefCollection(
        R.string.sc_pref_screen_advanced_theming_summary,
        listOf(
            ScPrefCategory(io.element.android.libraries.ui.strings.R.string.common_light, null, listOf(
                BUBBLE_BG_LIGHT_INCOMING,
                BUBBLE_BG_LIGHT_OUTGOING,
            )),
            ScPrefCategory(io.element.android.libraries.ui.strings.R.string.common_dark, null, listOf(
                BUBBLE_BG_DARK_INCOMING,
                BUBBLE_BG_DARK_OUTGOING,
            )),
        )
    )

    val scTweaks = ScPrefScreen(R.string.sc_pref_tweaks_title, null, listOf<AbstractScPref>(
        ScPrefScreen(R.string.sc_pref_category_general, R.string.sc_pref_category_general_summary, listOf(
            ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
                SC_THEME,
                EL_TYPOGRAPHY,
                TWO_INITIALS_AVATAR_FALLBACK,
                ScPrefScreen(R.string.sc_pref_screen_advanced_theming_title, R.string.sc_pref_screen_advanced_theming_summary, listOf(
                    SC_RESTORE_ADVANCED_THEME_DEFAULTS,
                    scTweaksAdvancedTheming,
                ))
            )),
            ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
                FAST_TRANSITIONS,
            )),
        )),
        ScPrefScreen(R.string.sc_pref_category_chat_overview, null, listOf(
            SC_OVERVIEW_LAYOUT,
            ELEMENT_ROOM_LIST_FILTERS,
            SNC_FAB,
            HIDE_INVITES,
            ScPrefCategory(R.string.sc_pref_category_chat_sorting, null, listOf(
                SORT_BY_UNREAD,
                SORT_WITH_SILENT_UNREAD,
                PIN_FAVORITES,
                BURY_LOW_PRIORITY,
            )),
            ScPrefCategory(R.string.sc_pref_category_unread_counts, null, listOf(
                RENDER_SILENT_UNREAD,
                DUAL_MENTION_UNREAD_COUNTS,
            )),
        )),
        ScPrefScreen(R.string.sc_pref_category_spaces, null, listOf(
            SPACE_NAV,
            SPACE_UNREAD_COUNTS,
            SPACE_SWIPE,
            COMPACT_ROOT_SPACES,
            ScPrefScreen(R.string.sc_pseudo_spaces_title, R.string.sc_pseudo_spaces_summary_experimental, listOf(
                ScPrefCategory(R.string.sc_pseudo_spaces_title, null, listOf(
                    PSEUDO_SPACE_ALL_ROOMS,
                    PSEUDO_SPACE_FAVORITES,
                    PSEUDO_SPACE_DMS,
                    PSEUDO_SPACE_GROUPS,
                    PSEUDO_SPACE_SPACELESS_GROUPS,
                    PSEUDO_SPACE_SPACELESS,
                    PSEUDO_SPACE_NOTIFICATIONS,
                    PSEUDO_SPACE_UNREAD,
                    PSEUDO_SPACE_INVITES,
                )),
                ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
                    PSEUDO_SPACE_HIDE_EMPTY_UNREAD,
                )),
            ), dependencies = SPACE_NAV.asDependencies())
        )),
        ScPrefScreen(R.string.sc_pref_category_timeline, null, listOf(
            SC_TIMELINE_LAYOUT,
            VIEW_REDACTIONS,
            //RENDER_INLINE_IMAGES,
            FLOATING_DATE,
            HIDE_CALL_TOOLBAR_ACTION,
            REPLY_PREVIEW_LINE_COUNT,
            LEGACY_MESSAGE_RENDERING,
            ScPrefCategory(R.string.sc_url_previews_title, null, listOf(
                URL_PREVIEWS,
                URL_PREVIEWS_IN_E2EE_ROOMS,
                URL_PREVIEWS_REQUIRE_EXPLICIT_LINKS,
            )),
            ScPrefCategory(R.string.sc_pref_category_pinned_messages, null, listOf(
                PINNED_MESSAGE_OVERLAY,
                PINNED_MESSAGE_TOOLBAR_ACTION,
            )),
            ScPrefCategory(R.string.sc_pref_category_message_context_menu, null, listOf(
                MESSAGE_CONTEXT_MENU_TEXT_SELECTABLE,
                FULLY_EXPAND_MESSAGE_MENU,
            )),
            ScPrefCategory(R.string.sc_pref_category_reactions, null, listOf(
                PREFER_FREEFORM_REACTIONS,
                PREFER_FULLSCREEN_REACTION_SHEET,
                ALWAYS_SHOW_REACTION_SEARCH_BAR,
            )),
        )),
        ScPrefScreen(R.string.sc_pref_category_notifications, null, listOf(
            NOTIFICATION_ONLY_ALERT_ONCE,
        )),
        ScPrefScreen(R.string.sc_pref_screen_experimental_title, R.string.sc_pref_screen_experimental_summary, listOf(
            ScPrefCategory(R.string.sc_pref_category_timeline, null, listOf(
                PL_DISPLAY_NAME,
                JUMP_TO_UNREAD,
            )),
            ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
                SHOW_SYNCING_INDICATOR,
                DEBOUNCE_OFFLINE_STATE,
            )),
        )),
        ScPrefCategory(R.string.sc_pref_category_debug_infos, null, listOf(
            SC_USER_CHANGED_SETTINGS,
            SC_PUSH_INFO,
        )),
        ScPrefCategoryCollapsed(SC_DEVELOPER_OPTIONS_CATEGORY_KEY, CommonStrings.common_developer_options, prefs = listOf(
            SC_DEV_QUICK_OPTIONS,
            VIEW_HIDDEN_EVENTS,
            READ_MARKER_DEBUG,
            SC_DANGER_ZONE,
            ScPrefScreen(R.string.sc_pref_chamber_of_secrets_title, null, listOf(
                ScDisclaimerPref("SC_CHAMBER_OF_SECRETS_DISCLAIMER", R.string.sc_pref_chamber_of_secrets_summary),
                CLIENT_GENERATED_UNREAD_COUNTS,
                SYNC_READ_RECEIPT_AND_MARKER,
                MARK_READ_REQUIRES_SEEN_UNREAD_LINE,
            ), dependencies = SC_DANGER_ZONE.asDependencies()),
            SC_RESTORE_DEFAULTS,
            SC_RESTORE_UPSTREAM,
            SC_RESTORE_AUTHORS_CHOICE,
        ), authorsChoice = true),
    ))

    val prefsToExcludeFromBatchSet = listOf(
        SC_DEVELOPER_OPTIONS_CATEGORY_KEY,
    )

    val devQuickTweaksOverview = listOf(
        ELEMENT_ROOM_LIST_FILTERS, // Used to be: ScUpstreamFeatureFlagAliasPref(FeatureFlags.RoomListFilters, R.string.sc_upstream_feature_flag_room_list_filters),
        SNC_FAB.copy(titleRes = R.string.sc_pref_snc_fab_title_short),
        RENDER_SILENT_UNREAD,
        // Timeline settings that don't reload live while the room is open
        ScPrefCategory(R.string.sc_pref_category_timeline_short, null, listOf(
            VIEW_REDACTIONS,
            VIEW_HIDDEN_EVENTS,
        )),
        ScPrefCategory(R.string.sc_pref_category_chat_sorting, null, listOf(
            SORT_BY_UNREAD,
            SORT_WITH_SILENT_UNREAD,
            PIN_FAVORITES,
            BURY_LOW_PRIORITY,
        )),
        ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            SC_OVERVIEW_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
            EL_TYPOGRAPHY,
            TWO_INITIALS_AVATAR_FALLBACK,
        )),
        ScPrefCategory(R.string.sc_pref_category_spaces, null, listOf(
            SPACE_NAV,
            COMPACT_ROOT_SPACES,
            ScPrefCategory(R.string.sc_pseudo_spaces_title, null, listOf(
                PSEUDO_SPACE_ALL_ROOMS,
                PSEUDO_SPACE_FAVORITES,
                PSEUDO_SPACE_DMS,
                PSEUDO_SPACE_GROUPS,
                PSEUDO_SPACE_SPACELESS_GROUPS.copy(titleRes = R.string.sc_pseudo_space_spaceless_groups_short),
                PSEUDO_SPACE_SPACELESS.copy(titleRes = R.string.sc_pseudo_space_spaceless_short),
                PSEUDO_SPACE_NOTIFICATIONS.copy(titleRes = R.string.sc_pseudo_space_notifications_short),
                PSEUDO_SPACE_UNREAD,
                PSEUDO_SPACE_INVITES,
                PSEUDO_SPACE_HIDE_EMPTY_UNREAD,
            ), dependencies = SPACE_NAV.asDependencies()),
        )),
        ScPrefCategory(R.string.sc_pref_category_misc, null, listOf(
            HIDE_INVITES,
            DUAL_MENTION_UNREAD_COUNTS.copy(titleRes = R.string.sc_pref_dual_mention_unread_counts_title_short),
            CLIENT_GENERATED_UNREAD_COUNTS,
            SYNC_READ_RECEIPT_AND_MARKER.copy(titleRes = R.string.sc_sync_read_receipt_and_marker_title_short),
            MARK_READ_REQUIRES_SEEN_UNREAD_LINE.copy(titleRes = R.string.sc_pref_mark_read_requires_seen_unread_line_title_short),
        )),
    )

    val devQuickTweaksTimeline = listOf(
        SC_THEME,
        EL_TYPOGRAPHY,
        SC_TIMELINE_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
        HIDE_CALL_TOOLBAR_ACTION,
        PINNED_MESSAGE_OVERLAY.copy(titleRes = R.string.sc_pref_pinned_message_overlay_title_short),
        PINNED_MESSAGE_TOOLBAR_ACTION.copy(titleRes = R.string.sc_pref_pinned_message_toolbar_title_short),
        //RENDER_INLINE_IMAGES,
        LEGACY_MESSAGE_RENDERING,
        MESSAGE_CONTEXT_MENU_TEXT_SELECTABLE.copy(titleRes = R.string.sc_pref_message_context_menu_text_selectable_title_short),
        ScPrefCategory(R.string.sc_pref_category_reactions, null, listOf(
            PREFER_FREEFORM_REACTIONS,
            PREFER_FULLSCREEN_REACTION_SHEET,
            ALWAYS_SHOW_REACTION_SEARCH_BAR,
        )),
        ScPrefCategory(R.string.sc_pref_screen_experimental_title, null, listOf(
            URL_PREVIEWS,
            URL_PREVIEWS_IN_E2EE_ROOMS.copy(titleRes = R.string.sc_url_previews_in_e2ee_rooms_title_short),
            URL_PREVIEWS_REQUIRE_EXPLICIT_LINKS.copy(titleRes = R.string.sc_url_previews_require_explicit_links_title_short),
            PL_DISPLAY_NAME,
            READ_MARKER_DEBUG,
        )),
    )
}
