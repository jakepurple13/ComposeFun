package com.programmersbox.composefun

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * A single select list setting. When pressed, a dialog will come up with radio buttons allowing the user to select an option.
 *
 * @param settingIcon the icon to the start of the row
 * @param summaryValue the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param dialogTitle the title for the dialog
 * @param cancelText the cancel text. The view state for the dialog is passed in. Setting it to false will dismiss the dialog.
 * @param confirmText the confirm text. The view state for the dialog is passed in. Setting it to false will dismiss the dialog.
 * @param value the current choice
 * @param options possible choices
 * @param viewText how the choice will appear to the user.
 * @param updateValue when the user presses on a new option
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun <T> ListSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    summaryValue: (@Composable () -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    dialogTitle: @Composable () -> Unit,
    cancelText: (@Composable (MutableState<Boolean>) -> Unit)? = null,
    confirmText: @Composable (MutableState<Boolean>) -> Unit,
    radioButtonColors: RadioButtonColors = RadioButtonDefaults.colors(),
    value: T,
    options: List<T>,
    viewText: (T) -> String = { it.toString() },
    updateValue: (T, MutableState<Boolean>) -> Unit
) {
    val dialogPopup = remember { mutableStateOf(false) }

    if (dialogPopup.value) {

        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { dialogPopup.value = false },
            title = dialogTitle,
            text = {
                LazyColumn {
                    items(options) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { updateValue(it, dialogPopup) }
                                .border(0.dp, Color.Transparent, RoundedCornerShape(20.dp))
                        ) {
                            RadioButton(
                                selected = it == value,
                                onClick = { updateValue(it, dialogPopup) },
                                modifier = Modifier.padding(8.dp),
                                colors = radioButtonColors
                            )
                            Text(
                                viewText(it),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            },
            confirmButton = { confirmText(dialogPopup) },
            dismissButton = cancelText?.let { { it(dialogPopup) } }
        )

    }

    PreferenceSetting(
        settingTitle = settingTitle,
        summaryValue = summaryValue,
        settingIcon = settingIcon,
        modifier = Modifier
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { dialogPopup.value = true }
            .then(modifier)
    )
}

/**
 * A multi select list setting. When pressed, a dialog will come up with checkboxes allowing the user to select multiple options.
 *
 * @param settingIcon the icon to the start of the row
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param dialogTitle the title for the dialog
 * @param cancelText the cancel text. The view state for the dialog is passed in. Setting it to false will dismiss the dialog.
 * @param confirmText the confirm text. The view state for the dialog is passed in. Setting it to false will dismiss the dialog.
 * @param values the current choices
 * @param options possible choices
 * @param viewText how the choice will appear to the user.
 * @param updateValue when the user presses on a new option
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun <T> MultiSelectListSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    settingSummary: (@Composable () -> Unit)? = null,
    dialogTitle: @Composable () -> Unit,
    cancelText: (@Composable (MutableState<Boolean>) -> Unit)? = null,
    confirmText: @Composable (MutableState<Boolean>) -> Unit,
    checkboxColors: CheckboxColors = CheckboxDefaults.colors(),
    values: List<T>,
    options: List<T>,
    viewText: (T) -> String = { it.toString() },
    updateValue: (T, Boolean) -> Unit
) {
    val dialogPopup = remember { mutableStateOf(false) }

    if (dialogPopup.value) {

        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { dialogPopup.value = false },
            title = dialogTitle,
            text = {
                LazyColumn {
                    items(options) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = rememberRipple(),
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { updateValue(it, it !in values) }
                                .border(0.dp, Color.Transparent, RoundedCornerShape(20.dp))
                        ) {
                            Checkbox(
                                checked = it in values,
                                onCheckedChange = { b -> updateValue(it, b) },
                                colors = checkboxColors,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                viewText(it),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            },
            confirmButton = { confirmText(dialogPopup) },
            dismissButton = cancelText?.let { { it(dialogPopup) } }
        )

    }

    PreferenceSetting(
        settingTitle = settingTitle,
        summaryValue = settingSummary,
        settingIcon = settingIcon,
        modifier = Modifier
            .clickable(
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { dialogPopup.value = true }
            .then(modifier)
    )
}

/**
 * A default setting to display information or have an action. Use [Modifier.clickable] to make the row clickable.
 *
 * @param settingIcon the icon to the start of the row
 * @param summaryValue the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param endIcon the icon to the end of the row
 */
@ExperimentalMaterialApi
@Composable
fun PreferenceSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    summaryValue: (@Composable () -> Unit)? = null,
    endIcon: (@Composable () -> Unit)? = null
) = DefaultPreferenceLayout(
    modifier = modifier,
    settingIcon = settingIcon,
    settingTitle = settingTitle,
    summaryValue = summaryValue,
    content = endIcon
)

/**
 * A switch setting.
 *
 * @param settingIcon the icon to the start of the row
 * @param summaryValue the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param value the current choice
 * @param updateValue when the user changes state
 */
@ExperimentalMaterialApi
@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    summaryValue: (@Composable () -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.colors(),
    value: Boolean,
    updateValue: (Boolean) -> Unit
) {
    DefaultPreferenceLayout(
        modifier = modifier.clickable(
            indication = rememberRipple(),
            interactionSource = remember { MutableInteractionSource() }
        ) { updateValue(!value) },
        settingIcon = settingIcon,
        settingTitle = settingTitle,
        summaryValue = summaryValue
    ) {
        Switch(
            checked = value,
            onCheckedChange = updateValue,
            colors = switchColors
        )
    }
}

/**
 * A checkbox setting
 *
 * @param settingIcon the icon to the start of the row
 * @param summaryValue the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param value the current choice
 * @param updateValue when the user changes state
 */
@ExperimentalMaterialApi
@Composable
fun CheckBoxSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    summaryValue: (@Composable () -> Unit)? = null,
    checkboxColors: CheckboxColors = CheckboxDefaults.colors(),
    value: Boolean,
    updateValue: (Boolean) -> Unit
) {
    DefaultPreferenceLayout(
        modifier = modifier.clickable(
            indication = rememberRipple(),
            interactionSource = remember { MutableInteractionSource() }
        ) { updateValue(!value) },
        settingIcon = settingIcon,
        settingTitle = settingTitle,
        summaryValue = summaryValue
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = updateValue,
            colors = checkboxColors
        )
    }
}

/**
 * A slider setting
 *
 * @param settingIcon the icon to the start of the row
 * @param settingSummary the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param sliderValue the current choice
 * @param updateValue when the user slides the slider
 * @param format change the [sliderValue] to a user readable string
 * @param range the range of the slider
 * @param onValueChangedFinished when the slider is done changing the value
 * @param steps the steps in between.
 */
@Composable
fun SliderSetting(
    modifier: Modifier = Modifier,
    sliderValue: Float,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    settingSummary: (@Composable () -> Unit)? = null,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
    format: (Float) -> String = { it.toInt().toString() },
    onValueChangedFinished: (() -> Unit)? = null,
    updateValue: (Float) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .then(modifier)
    ) {
        val (
            icon,
            info,
            slider,
            value
        ) = createRefs()

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(32.dp)
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        ) { settingIcon?.invoke(this) }

        Column(
            modifier = Modifier.constrainAs(info) {
                top.linkTo(parent.top)
                end.linkTo(parent.end, 8.dp)
                start.linkTo(icon.end)
                width = Dimension.fillToConstraints
            }
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
            ) { settingTitle() }
            settingSummary?.let {
                ProvideTextStyle(MaterialTheme.typography.body2.copy(textAlign = TextAlign.Start)) { it() }
            }
        }

        Slider(
            value = sliderValue,
            onValueChange = updateValue,
            onValueChangeFinished = onValueChangedFinished,
            valueRange = range,
            steps = steps,
            colors = colors,
            modifier = Modifier.constrainAs(slider) {
                top.linkTo(info.bottom)
                end.linkTo(value.start)
                start.linkTo(icon.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            format(sliderValue),
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .constrainAs(value) {
                    end.linkTo(parent.end)
                    start.linkTo(slider.end)
                    centerVerticallyTo(slider)
                }
                .padding(horizontal = 16.dp)
        )
    }
}

/**
 * A [PreferenceSetting] with a dropdown icon at the end, using [ShowWhen] to show or hide multiple other settings.
 *
 * @param settingIcon the icon to the start of the row
 * @param summaryValue the value which will be placed under the title. The default style is [MaterialTheme.typography.body2]
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 * @param content other settings or content
 */
@Composable
fun ShowMoreSetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    summaryValue: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        var showMore by remember { mutableStateOf(false) }
        DefaultPreferenceLayout(
            modifier = modifier.clickable(
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            ) { showMore = !showMore },
            settingIcon = settingIcon,
            settingTitle = settingTitle,
            summaryValue = summaryValue
        ) {
            Icon(
                Icons.Default.ArrowDropDown,
                null,
                modifier = Modifier.rotate(animateFloatAsState(targetValue = if (showMore) 180f else 0f).value)
            )
        }
        ShowWhen(showMore) { content() }
    }
}

@Composable
private fun DefaultPreferenceLayout(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit,
    summaryValue: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val (icon, text, endIcon) = createRefs()

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(32.dp)
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    centerVerticallyTo(parent)
                },
            contentAlignment = Alignment.Center
        ) { settingIcon?.invoke(this) }

        Column(
            modifier = Modifier.constrainAs(text) {
                start.linkTo(icon.end, 8.dp)
                end.linkTo(endIcon.start, 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                centerVerticallyTo(parent)
                width = Dimension.fillToConstraints
            }
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
            ) { settingTitle() }
            summaryValue?.let {
                ProvideTextStyle(MaterialTheme.typography.body2.copy(textAlign = TextAlign.Start)) { it() }
            }
        }

        Box(
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(endIcon) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    centerVerticallyTo(parent)
                }
        ) { content?.invoke() }
    }
}

/**
 * A category setting that is bold.
 *
 * @param settingIcon the icon to the start of the row
 * @param settingTitle the title for the row. The default style is [MaterialTheme.typography.body1]
 */
@ExperimentalMaterialApi
@Composable
fun CategorySetting(
    modifier: Modifier = Modifier,
    settingIcon: (@Composable BoxScope.() -> Unit)? = null,
    settingTitle: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colors.primary
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
        ) {
            val (icon, text) = createRefs()

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp)
                    .constrainAs(icon) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        centerVerticallyTo(parent)
                    },
                contentAlignment = Alignment.Center
            ) { settingIcon?.invoke(this) }

            Column(
                modifier = Modifier.constrainAs(text) {
                    start.linkTo(icon.end, 8.dp)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    centerVerticallyTo(parent)
                    width = Dimension.fillToConstraints
                }
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.body1.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Start)
                ) { settingTitle() }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun SettingsScreen(navController: NavController = rememberNavController()) {
    ScaffoldTop(screen = Screen.SettingsScreen, navController = navController) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .verticalScroll(rememberScrollState())
        ) {
            CategorySetting { Text("Category") }

            var switchSetting by remember { mutableStateOf(false) }
            SwitchSetting(settingTitle = { Text("Switch") }, value = switchSetting, updateValue = { switchSetting = it })

            Divider()

            var checkboxSetting by remember { mutableStateOf(false) }
            CheckBoxSetting(settingTitle = { Text("CheckBox") }, value = checkboxSetting, updateValue = { checkboxSetting = it })

            Divider()

            var sliderSetting by remember { mutableStateOf(0f) }
            SliderSetting(
                sliderValue = sliderSetting,
                settingTitle = { Text("Slider") },
                range = 0f..100f,
                updateValue = { sliderSetting = it }
            )

            var listSetting by remember { mutableStateOf(1) }

            ListSetting(
                settingTitle = { Text("List") },
                dialogTitle = { Text("Choose") },
                confirmText = { Button(onClick = { it.value = false }) { Text("Confirm") } },
                value = listSetting,
                options = listOf(1, 2, 3, 4, 5),
                updateValue = { it, _ -> listSetting = it },
                summaryValue = { Text(listSetting.toString()) }
            )

            val multiListSetting = remember { mutableStateListOf(1, 2) }

            MultiSelectListSetting(
                settingTitle = { Text("MultiList") },
                dialogTitle = { Text("Choose") },
                confirmText = { Button(onClick = { it.value = false }) { Text("Confirm") } },
                values = multiListSetting,
                options = listOf(1, 2, 3, 4, 5),
                updateValue = { it, b -> if (b) multiListSetting.add(it) else multiListSetting.remove(it) },
                settingSummary = { Text(multiListSetting.fastMap { it }.toString()) }
            )

            Divider()

            ShowMoreSetting(settingTitle = { Text("Show More") }) {
                Column {
                    PreferenceSetting(settingTitle = { Text("Setting") })
                    PreferenceSetting(settingTitle = { Text("Setting") })
                    PreferenceSetting(settingTitle = { Text("Setting") })
                }
            }

            Divider()

        }
    }
}
