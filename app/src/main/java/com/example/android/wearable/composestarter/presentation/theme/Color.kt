/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.composestarter.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val White = Color(0xFFFFFFFF)
val Red400 = Color(0xFFCF6679)
val Charcoal = Color(0x101820FF)
val Yellow = Color(0xFEE715FF)

internal val wearColorPalette: Colors = Colors(
    primary = White,
    primaryVariant = White,
    secondary = Yellow,
    secondaryVariant = Yellow,
    error = Red400,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.Black
)
