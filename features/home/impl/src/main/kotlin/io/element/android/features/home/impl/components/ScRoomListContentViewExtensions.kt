package io.element.android.features.home.impl.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chat.schildi.features.home.spaces.SpaceListDataSource
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun ScSpaceEmptyView(
    selectedSpace: SpaceListDataSource.AbstractSpaceHierarchyItem?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        EmptyScaffold(
            title = emptySpaceTitleFromSelectedSpace(selectedSpace),
            subtitle = chat.schildi.lib.R.string.sc_space_empty_summary,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun emptySpaceTitleFromSelectedSpace(selectedSpace: SpaceListDataSource.AbstractSpaceHierarchyItem?): Int {
    return when (selectedSpace) {
        // Re-use some upstream strings where it fits
        is SpaceListDataSource.FavoritesPseudoSpaceItem -> R.string.screen_roomlist_filter_favourites_empty_state_title
        is SpaceListDataSource.DmsPseudoSpaceItem -> R.string.screen_roomlist_filter_people_empty_state_title
        //is SpaceListDataSource.GroupsPseudoSpaceItem -> R.string.screen_roomlist_filter_rooms_empty_state_title
        //is SpaceListDataSource.SpacelessGroupsPseudoSpaceItem -> R.string.screen_roomlist_filter_rooms_empty_state_title
        is SpaceListDataSource.UnreadPseudoSpaceItem -> R.string.screen_roomlist_filter_unreads_empty_state_title
        is SpaceListDataSource.NotificationsPseudoSpaceItem -> R.string.screen_roomlist_filter_unreads_empty_state_title
        else -> chat.schildi.lib.R.string.sc_space_empty_title
    }
}

// Copied from somewhere upstream, was private
@Composable
private fun EmptyScaffold(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    modifier: Modifier = Modifier,
    action: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(title),
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(subtitle),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        action?.invoke(this)
    }
}
