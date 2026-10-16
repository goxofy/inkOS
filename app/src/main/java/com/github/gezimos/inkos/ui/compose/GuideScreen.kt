package com.github.gezimos.inkos.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import com.github.gezimos.inkos.ui.compose.inkOsSafeDrawingPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwipeVertical
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.gezimos.inkos.R
import com.github.gezimos.inkos.data.Constants
import com.github.gezimos.inkos.data.Prefs
import com.github.gezimos.inkos.style.SettingsTheme
import com.github.gezimos.inkos.style.Theme
import com.github.gezimos.inkos.style.rememberScreenScale
import com.github.gezimos.inkos.style.scaled

class GuideFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GuideScreen.Show(
                    onBack = { findNavController().popBackStack() },
                    onHome = { findNavController().popBackStack(R.id.mainFragment, false) }
                )
            }
        }
    }
}

object GuideScreen {

    enum class GuideCategory(val totalPages: Int) {
        Touch(4), DPadT9(2), Qwerty(1), Search(2)
    }

    @Composable
    fun Show(
        onBack: () -> Unit = {},
        onHome: () -> Unit = {}
    ) {
        val context = LocalContext.current
        val prefs = remember { Prefs(context) }

        val screenScaleRaw = com.github.gezimos.inkos.style.detectScaleMode(context).let { mode ->
            if (prefs.uiScaleMode != 0) com.github.gezimos.inkos.style.UiScaleMode.fromId(prefs.uiScaleMode).scale
            else mode.scale
        }
        val settingsSize = (prefs.settingsSize - 3)
        val titleFontSize = (settingsSize * 1.5f * screenScaleRaw).sp
        val bodyFontSize = (settingsSize * 1.2f * screenScaleRaw).sp
        val bigTitleSize = (settingsSize * 3f * screenScaleRaw).sp

        val systemDark = isSystemInDarkTheme()
        val isDark = when (prefs.appTheme) {
            Constants.Theme.Dark -> true
            Constants.Theme.Light -> false
            Constants.Theme.System -> systemDark
        }
        var category by remember { mutableStateOf<GuideCategory?>(null) }
        var page by remember { mutableStateOf(0) }
        val totalPages = category?.totalPages ?: 1

        BackHandler(enabled = category != null) {
            category = null
            page = 0
        }

        SettingsTheme(isDark = isDark) {
            val textColor = Theme.colors.text
            val bgColor = Theme.colors.background
            val screenScale = rememberScreenScale()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .inkOsSafeDrawingPadding()
            ) {
                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp.scaled(screenScale), vertical = 32.dp.scaled(screenScale))
                ) {
                    // Big title — same for all pages
                    Text(
                        text = stringResource(R.string.guide),
                        style = SettingsTheme.typography.title,
                        fontSize = bigTitleSize,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        lineHeight = (bigTitleSize.value * 1.05f).sp
                    )

                    // Thick bar
                    Spacer(modifier = Modifier.height(16.dp.scaled(screenScale)))
                    Box(
                        modifier = Modifier
                            .width(60.dp.scaled(screenScale))
                            .height(5.dp)
                            .background(textColor)
                    )

                    Spacer(modifier = Modifier.height(32.dp.scaled(screenScale)))

                    // Page-specific content
                    when (category) {
                        null -> MenuContent(titleFontSize, bodyFontSize, screenScale, textColor) { picked ->
                            category = picked
                            page = 0
                        }
                        GuideCategory.Touch -> when (page) {
                            0 -> HomePageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                            1 -> GesturesPageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                            2 -> HomeNotificationsPageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                            3 -> WidgetsPageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                        }
                        GuideCategory.DPadT9 -> when (page) {
                            0 -> DPadHomePageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                            1 -> DPadLettersPageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                        }
                        GuideCategory.Qwerty -> QwertyPageContent(titleFontSize, bodyFontSize, screenScale, textColor)
                        GuideCategory.Search -> when (page) {
                            0 -> SearchPage1Content(titleFontSize, bodyFontSize, screenScale, textColor)
                            1 -> SearchPage2Content(titleFontSize, bodyFontSize, screenScale, textColor)
                        }
                    }

                    // Page subtitle at the bottom (APP VERSION style)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = when (category) {
                            null -> stringResource(R.string.guide_pick_input_method)
                            GuideCategory.Touch -> when (page) {
                                0 -> stringResource(R.string.guide_home_screen_interactions)
                                1 -> stringResource(R.string.guide_home_gestures)
                                2 -> stringResource(R.string.guide_home_notifications)
                                3 -> stringResource(R.string.guide_home_widgets)
                                else -> ""
                            }
                            GuideCategory.DPadT9 -> when (page) {
                                0 -> stringResource(R.string.guide_home_screen)
                                1 -> stringResource(R.string.shortcut_letters_short)
                                else -> ""
                            }
                            GuideCategory.Qwerty -> stringResource(R.string.guide_qwerty)
                            GuideCategory.Search -> stringResource(R.string.tab_search)
                        },
                        style = SettingsTheme.typography.title,
                        fontSize = (titleFontSize.value * 0.7f).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 2.sp
                    )
                }

                // Separator line above footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp.scaled(screenScale))
                        .background(textColor)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp.scaled(screenScale)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (category == null) {
                        // Menu footer: BACK | HOME
                        val backMenuInteraction = remember { MutableInteractionSource() }
                        val backMenuFocused = backMenuInteraction.collectIsFocusedAsState().value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (backMenuFocused) textColor else bgColor)
                                .clickable(
                                    interactionSource = backMenuInteraction,
                                    indication = null
                                ) { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.btn_back_upper),
                                style = SettingsTheme.typography.title,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (backMenuFocused) bgColor else textColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.5.dp.scaled(screenScale))
                                .fillMaxHeight()
                                .background(textColor)
                        )

                        Box(
                            modifier = Modifier.width(54.dp.scaled(screenScale)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1/1",
                                style = SettingsTheme.typography.body,
                                fontSize = (titleFontSize.value * 0.7f).sp,
                                color = textColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.5.dp.scaled(screenScale))
                                .fillMaxHeight()
                                .background(textColor)
                        )

                        val homeInteraction = remember { MutableInteractionSource() }
                        val homeFocused = homeInteraction.collectIsFocusedAsState().value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (homeFocused) textColor else bgColor)
                                .clickable(
                                    interactionSource = homeInteraction,
                                    indication = null
                                ) { onHome() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.guide_home_button),
                                style = SettingsTheme.typography.title,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (homeFocused) bgColor else textColor
                            )
                        }
                    } else {
                        // BACK
                        val backInteraction = remember { MutableInteractionSource() }
                        val backFocused = backInteraction.collectIsFocusedAsState().value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (backFocused) textColor else bgColor)
                                .clickable(
                                    interactionSource = backInteraction,
                                    indication = null
                                ) {
                                    if (page > 0) page-- else {
                                        category = null
                                        page = 0
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.btn_back_upper),
                                style = SettingsTheme.typography.title,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (backFocused) bgColor else textColor
                            )
                        }

                        // Vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.5.dp.scaled(screenScale))
                                .fillMaxHeight()
                                .background(textColor)
                        )

                        // Page indicator
                        Box(
                            modifier = Modifier.width(54.dp.scaled(screenScale)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${page + 1}/$totalPages",
                                style = SettingsTheme.typography.body,
                                fontSize = (titleFontSize.value * 0.7f).sp,
                                color = textColor
                            )
                        }

                        // Vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.5.dp.scaled(screenScale))
                                .fillMaxHeight()
                                .background(textColor)
                        )

                        // NEXT / DONE
                        val nextInteraction = remember { MutableInteractionSource() }
                        val nextFocused = nextInteraction.collectIsFocusedAsState().value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (nextFocused) textColor else bgColor)
                                .clickable(
                                    interactionSource = nextInteraction,
                                    indication = null
                                ) {
                                    if (page < totalPages - 1) {
                                        page++
                                    } else {
                                        category = null
                                        page = 0
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (page < totalPages - 1) stringResource(R.string.btn_next) else stringResource(R.string.done),
                                style = SettingsTheme.typography.title,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = if (nextFocused) bgColor else textColor
                            )
                        }
                    }
                }

                // Bottom border when nav bar is visible
                if (WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp.scaled(screenScale))
                            .background(textColor)
                    )
                }
            }
        }
    }

    @Composable
    private fun GuideItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp.scaled(screenScale)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(28.dp.scaled(screenScale))
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = SettingsTheme.typography.title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = description,
                    style = SettingsTheme.typography.body,
                    fontSize = bodyFontSize,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }

    @Composable
    private fun HomePageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.Settings, stringResource(R.string.guide_pinch_title), stringResource(R.string.guide_pinch_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.TouchApp, stringResource(R.string.guide_longpress_app_title), stringResource(R.string.guide_longpress_app_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Widgets, stringResource(R.string.guide_tap_clock_title), stringResource(R.string.guide_tap_clock_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.SwipeVertical, stringResource(R.string.guide_short_swipes_title), stringResource(R.string.guide_short_swipes_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun GesturesPageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.ArrowUpward, stringResource(R.string.guide_swipe_up_title), stringResource(R.string.guide_swipe_up_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.ArrowDownward, stringResource(R.string.guide_swipe_down_title), stringResource(R.string.guide_swipe_down_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.ArrowBack, stringResource(R.string.guide_swipe_left_title), stringResource(R.string.guide_swipe_left_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.ArrowForward, stringResource(R.string.guide_swipe_right_title), stringResource(R.string.guide_swipe_right_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun HomeNotificationsPageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.Emergency, stringResource(R.string.guide_signal_asterisk_title), stringResource(R.string.guide_signal_asterisk_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.SpeakerNotes, stringResource(R.string.guide_signal_title), stringResource(R.string.guide_signal_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Lyrics, "Spotify", stringResource(R.string.guide_spotify_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.FilterList, stringResource(R.string.guide_allow_list_title), stringResource(R.string.guide_allow_list_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun WidgetsPageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.FormatQuote, stringResource(R.string.bottom_widget_quote), stringResource(R.string.guide_quote_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.CalendarMonth, stringResource(R.string.bottom_widget_events), stringResource(R.string.guide_events_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Widgets, stringResource(R.string.bottom_widget_android), stringResource(R.string.guide_android_widget_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Timer, stringResource(R.string.bottom_widget_total_usage), stringResource(R.string.guide_total_usage_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun MenuContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color,
        onPick: (GuideCategory) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            MenuRow(Icons.Rounded.TouchApp, stringResource(R.string.guide_menu_touch_title), stringResource(R.string.guide_menu_touch_desc), titleFontSize, bodyFontSize, screenScale, textColor) { onPick(GuideCategory.Touch) }
            MenuRow(Icons.Rounded.Dialpad, stringResource(R.string.guide_menu_dpad_title), stringResource(R.string.guide_menu_dpad_desc), titleFontSize, bodyFontSize, screenScale, textColor) { onPick(GuideCategory.DPadT9) }
            MenuRow(Icons.Rounded.Keyboard, stringResource(R.string.guide_menu_qwerty_title), stringResource(R.string.guide_menu_qwerty_desc), titleFontSize, bodyFontSize, screenScale, textColor) { onPick(GuideCategory.Qwerty) }
            MenuRow(Icons.Rounded.Search, stringResource(R.string.search), stringResource(R.string.guide_menu_search_desc), titleFontSize, bodyFontSize, screenScale, textColor) { onPick(GuideCategory.Search) }
        }
    }

    @Composable
    private fun MenuRow(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        description: String,
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color,
        onClick: () -> Unit
    ) {
        val interaction = remember { MutableInteractionSource() }
        val focused = interaction.collectIsFocusedAsState().value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interaction, indication = null) { onClick() },
            horizontalArrangement = Arrangement.spacedBy(16.dp.scaled(screenScale)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(28.dp.scaled(screenScale))
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = SettingsTheme.typography.title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = description,
                    style = SettingsTheme.typography.body,
                    fontSize = bodyFontSize,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }

    @Composable
    private fun DPadHomePageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.Apps, stringResource(R.string.guide_dpad_longpress9_title), stringResource(R.string.guide_dpad_longpress9_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.TouchApp, stringResource(R.string.guide_dpad_longpress_center_title), stringResource(R.string.guide_longpress_app_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.SwipeVertical, stringResource(R.string.guide_dpad_updown_title), stringResource(R.string.guide_dpad_updown_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.SwapHoriz, stringResource(R.string.guide_dpad_leftright_title), stringResource(R.string.guide_dpad_leftright_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun DPadLettersPageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.OpenInNew, stringResource(R.string.guide_letters_center_title), stringResource(R.string.guide_letters_center_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Close, stringResource(R.string.guide_letters_backspace_title), stringResource(R.string.guide_letters_backspace_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.ClearAll, stringResource(R.string.guide_letters_longpress_title), stringResource(R.string.guide_letters_longpress_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.SwipeVertical, stringResource(R.string.guide_dpad_updown_title), stringResource(R.string.guide_letters_updown_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun QwertyPageContent(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.Search, stringResource(R.string.guide_qwerty_search_title), stringResource(R.string.guide_qwerty_search_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Keyboard, stringResource(R.string.guide_qwerty_dpad_title), stringResource(R.string.guide_qwerty_dpad_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun SearchPage1Content(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.Apps, stringResource(R.string.guide_search_apps_title), stringResource(R.string.guide_search_apps_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Bolt, stringResource(R.string.bottom_widget_shortcuts), stringResource(R.string.guide_search_shortcuts_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Contacts, stringResource(R.string.guide_search_contacts_title), stringResource(R.string.guide_search_contacts_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Folder, stringResource(R.string.guide_search_files_title), stringResource(R.string.guide_search_files_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

    @Composable
    private fun SearchPage2Content(
        titleFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
        screenScale: Float,
        textColor: androidx.compose.ui.graphics.Color
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp.scaled(screenScale))) {
            GuideItem(Icons.Rounded.MusicNote, stringResource(R.string.guide_search_music_title), stringResource(R.string.guide_search_music_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Language, stringResource(R.string.guide_search_web_title), stringResource(R.string.guide_search_web_desc), titleFontSize, bodyFontSize, screenScale, textColor)
            GuideItem(Icons.Rounded.Link, stringResource(R.string.guide_search_url_title), stringResource(R.string.guide_search_url_desc), titleFontSize, bodyFontSize, screenScale, textColor)
        }
    }

}
