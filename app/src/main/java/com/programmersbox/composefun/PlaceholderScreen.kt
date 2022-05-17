package com.programmersbox.composefun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import com.google.accompanist.placeholder.material.placeholder as mplaceholder

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(navController: NavController) {
    ScaffoldTop(
        screen = Screen.PlaceholderScreen,
        navController = navController,
    ) { p ->
        LazyColumn(
            contentPadding = p,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .mplaceholder(true)
                            )
                        },
                        icon = { Icon(Icons.Default.BrokenImage, null, modifier = Modifier.mplaceholder(true)) }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .mplaceholder(true, highlight = PlaceholderHighlight.shimmer())
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.mplaceholder(true, highlight = PlaceholderHighlight.shimmer())
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .mplaceholder(true, highlight = PlaceholderHighlight.fade())
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.mplaceholder(true, highlight = PlaceholderHighlight.fade())
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = MaterialTheme.colors.primaryVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = MaterialTheme.colors.primaryVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            )
                        }
                    )
                }
            }

            items(2) {
                ElevatedCard {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = M3MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = M3MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = MaterialTheme.colors.primaryVariant,
                                        shape = RoundedCornerShape(4.dp),
                                        highlight = PlaceholderHighlight.shimmer()
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = MaterialTheme.colors.primaryVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer()
                                )
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = M3MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp),
                                        highlight = PlaceholderHighlight.shimmer()
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = M3MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.shimmer()
                                )
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = MaterialTheme.colors.primaryVariant,
                                        shape = RoundedCornerShape(4.dp),
                                        highlight = PlaceholderHighlight.fade()
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = MaterialTheme.colors.primaryVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.fade()
                                )
                            )
                        }
                    )
                }
            }

            items(2) {
                Card {
                    ListItem(
                        text = {
                            Text(
                                "Hello",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .placeholder(
                                        true,
                                        color = M3MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp),
                                        highlight = PlaceholderHighlight.fade()
                                    )
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                modifier = Modifier.placeholder(
                                    true,
                                    color = M3MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp),
                                    highlight = PlaceholderHighlight.fade()
                                )
                            )
                        }
                    )
                }
            }

        }
    }
}