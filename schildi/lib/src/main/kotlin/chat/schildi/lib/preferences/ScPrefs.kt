package chat.schildi.lib.preferences

import androidx.compose.ui.res.stringArrayResource
import chat.schildi.lib.R
import io.element.android.libraries.ui.strings.CommonStrings

object ScPrefs {

    object SpaceUnreadCountMode {
        const val MESSAGES = "MESSAGES"
        const val CHATS = "CHATS"
        const val HIDE = "HIDE"
    }

    // Appearance
    val SC_THEME = ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title, upstreamChoice = false)
    val EL_TYPOGRAPHY = ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary, upstreamChoice = true)

    // General behavior
    val FAST_TRANSITIONS = ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary, upstreamChoice = false)

    // Chat overview
    val COMPACT_APP_BAR = ScBoolPref("COMPACT_APP_BAR", true, R.string.sc_pref_compact_app_bar_title, R.string.sc_pref_compact_app_bar_summary, upstreamChoice = false)
    val SC_OVERVIEW_LAYOUT = ScBoolPref("SC_OVERVIEW_LAYOUT", true, R.string.sc_pref_sc_overview_layout_title, upstreamChoice = false)
    val CLIENT_GENERATED_UNREAD_COUNTS = ScBoolPref("CLIENT_GENERATED_UNREAD_COUNTS", false, R.string.sc_client_generated_unread_counts_title, R.string.sc_client_generated_unread_counts_summary, upstreamChoice = true, authorsChoice = false)
    val SPACE_NAV = ScBoolPref("SPACE_NAV", false, R.string.sc_space_nav_title, R.string.sc_space_nav_summary, upstreamChoice = false, authorsChoice = true)
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

    // Timeline
    val SC_TIMELINE_LAYOUT = ScBoolPref("SC_TIMELINE_LAYOUT", true, R.string.sc_pref_sc_timeline_layout_title, upstreamChoice = false)
    val FLOATING_DATE = ScBoolPref("FLOATING_DATE", true, R.string.sc_pref_sc_floating_date_title, R.string.sc_pref_sc_floating_date_summary, upstreamChoice = false)
    val PL_DISPLAY_NAME = ScBoolPref("PL_DISPLAY_NAME", false, R.string.sc_pref_pl_display_name_title, R.string.sc_pref_pl_display_name_summary_warning, authorsChoice = false, upstreamChoice = false, dependencies = SC_THEME.asDependencies())
    val SYNC_READ_RECEIPT_AND_MARKER = ScBoolPref("SYNC_READ_RECEIPT_AND_MARKER", false, R.string.sc_sync_read_receipt_and_marker_title, R.string.sc_sync_read_receipt_and_marker_summary, authorsChoice = true)

    // Developer options
    val SC_DEV_QUICK_OPTIONS = ScBoolPref("SC_DEV_QUICK_OPTIONS", false, R.string.sc_pref_dev_quick_options, authorsChoice = true)
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
            EL_TYPOGRAPHY,
        )),
        ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
            FAST_TRANSITIONS,
        )),
        ScPrefCategory(R.string.sc_pref_category_chat_overview, null, listOf(
            SC_OVERVIEW_LAYOUT,
            COMPACT_APP_BAR,
        )),
        ScPrefCategory(R.string.sc_pref_category_spaces, null, listOf(
            SPACE_NAV,
            SPACE_UNREAD_COUNTS,
            SPACE_SWIPE,
        )),
        ScPrefCategory(R.string.sc_pref_category_timeline, null, listOf(
            SC_TIMELINE_LAYOUT,
            FLOATING_DATE,
        )),
        ScPrefCategory(R.string.sc_pref_category_misc, null, listOf(
            ScPrefScreen(R.string.sc_pref_screen_experimental_title, R.string.sc_pref_screen_experimental_summary, listOf(
                PL_DISPLAY_NAME,
                SYNC_READ_RECEIPT_AND_MARKER,
                CLIENT_GENERATED_UNREAD_COUNTS,
            )),
        )),
        ScPrefCategory(CommonStrings.common_developer_options, null, listOf(
            SC_DEV_QUICK_OPTIONS,
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
        SC_THEME,
        SC_OVERVIEW_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
        EL_TYPOGRAPHY,
        SPACE_NAV,
        CLIENT_GENERATED_UNREAD_COUNTS,
    )

    val devQuickTweaksTimeline = listOf(
        SC_THEME,
        EL_TYPOGRAPHY,
        SC_TIMELINE_LAYOUT.copy(titleRes = R.string.sc_pref_sc_layout_title),
        ScPrefCategory(R.string.sc_pref_screen_experimental_title, null, listOf(
            PL_DISPLAY_NAME,
        )),
    )
}
