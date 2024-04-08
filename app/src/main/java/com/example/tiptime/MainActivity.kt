/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    //Add a TextField that accepts a value named parameter set to an empty string and an onValueChange named parameter set to an empty lambda expression
    var amountInput by remember { mutableStateOf("") } //so that Compose knows to track the amountInput state and then pass in a "0" string, which is the initial default value for the amountInput state variable.
    // can also be written like this >>> var amountInput: MutableState<String> = mutableStateOf("0") <<<
    var tipInput by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) } //This is the variable for the Switch composable state, and false will be the default state.

    val amount = amountInput.toDoubleOrNull() ?: 0.0 //parses a string as a Double number and returns the result or null if the string isn't a valid representation of a number. ?: Elvis operator that returns a 0.0 value when amountInput is null.
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0 //Define a val named tipPercent that converts the tipInput variable to a Double type. Use an Elvis operator and return 0, if the value is null. This value could be null if the text field is empty.

    val tip = calculateTip(amount, tipPercent, roundUp)
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),// scrolls the screen.
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start)
        )
        //This makes EditNumberField stateless. You hoisted the UI state to its ancestor, TipTimeLayout().
        // The TipTimeLayout() is the state(amountInput) owner now.
        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = amountInput, //value parameter is a text box that displays the string value you pass. this also triggers a recomposition when its value changes.
            onValueChanged = { amountInput = it }, //onValueChange parameter is the lambda callback that's triggered when the user enters text in the text box
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )
        //adds another text box for the custom tip percentage.
        EditNumberField(
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            label = R.string.how_was_the_service,
            leadingIcon = R.drawable.percent,
            value = tipInput,
            onValueChanged = { tipInput = it },
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@Composable
//hoist the state by adding the value and onValueChange parameters
fun EditNumberField(
    @StringRes label: Int,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChanged: (String) -> Unit, //onValueChanged parameter is of (String) -> Unit type, so it's a function that takes a String value as input and has no return value.
    modifier: Modifier = Modifier) {
    TextField(
        value = value,
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) },
        onValueChange = onValueChanged,
        singleLine = true, //This condenses the text box to a single, horizontally scrollable line from multiple lines.
        label = { Text(stringResource(label)) }, //call the Text function that accepts a stringResource
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onRoundUpChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier) {
    Row(

        modifier = modifier
            .fillMaxWidth()
            .size(48.dp)
            .wrapContentWidth(Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //add a Text composable that uses the R.string.round_up_tip string resource to display a Round up tip? string
        Text(text = stringResource(R.string.round_up_tip))
        //add a Switch composable, and pass a checked named parameter set it to roundUp and an onCheckedChange named parameter set it to onRoundUpChanged.
        Switch(
            checked = roundUp,
            onCheckedChange = onRoundUpChanged,
        )
    }
}

/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
@VisibleForTesting //This makes the method public, but indicates to others that it's only public for testing purposes.
internal fun calculateTip(
    amount: Double,
    tipPercent: Double = 15.0,
    //add a roundUp parameter of Boolean type:
    roundUp: Boolean
): String {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return NumberFormat.getCurrencyInstance().format(tip)
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}

/*
Styles and themes are a collection of attributes that specifies the appearance for a single UI element.
A style can specify attributes such as font color, font size, background color, and much more which can
be applied for the entire app. Later codelabs will cover how to implement these in your app.

The Composition is a description of the UI built by Compose when it executes composables. Compose apps
call composable functions to transform data into UI. If a state change happens, Compose re-executes the
affected composable functions with the new state, which creates an updated UI—this is called
recomposition. Compose schedules a recomposition for you.

You use the State and MutableState types in Compose to make state in your app observable, or tracked,
by Compose. The State type is immutable, so you can only read the value in it, while the MutableState
type is mutable. You can use the mutableStateOf() function to create an observable MutableState. It
receives an initial value as a parameter that is wrapped in a State object, which then makes its value
observable.

Composable functions can store an object across recompositions with the remember. A value computed by
the remember function is stored in the Composition during initial composition and the stored value is
returned during recomposition. Usually remember and mutableStateOf functions are used together in
composable functions to have the state and its updates be reflected properly in the UI.

Now the empty string is the initial default value for the amountInput variable. by is a Kotlin property
delegation. The default getter and setter functions for the amountInput property are delegated to the
remember class's getter and setter functions, respectively.

Android provides an option to configure the keyboard displayed on the screen to enter digits, email
addresses, URLs, and passwords, to name a few.

The toDoubleOrNull() function is a predefined Kotlin function that parses a string as a Double number
and returns the result or null if the string isn't a valid representation of a number.

The ?: Elvis operator returns the expression that precedes it if the value isn't null and the expression
that proceeds it when the value is null. It lets you write this code more idiomatically.

A stateless composable is a composable that doesn't store its own state. It displays whatever state
it's given as input arguments.

You should hoist the state when you need to:
- Share the state with multiple composable functions.
- Create a stateless composable that can be reused in your app.

A stateless composable is a composable that doesn't have a state, meaning it doesn't hold, define, or
modify a new state. On the other hand, a stateful composable is a composable that owns a piece of state
that can change over time.

When applied to composables, this often means introducing two parameters to the composable:
- A value: T parameter, which is the current value to display.
- n onValueChange: (T) -> Unit – callback lambda, which is triggered when the value changes so that the
  state can be updated elsewhere, such as when a user enters some text in the text box.

The @StringRes annotation is a type-safe way to use string resources. It indicates that the integer to
be passed is a string resource from the values/strings.xml file. These annotations are useful to developers
who work on your code and for code-inspection tools like lint in Android Studio.

- ImeAction.Search
  Used when the user wants to execute a search.
- ImeAction.Send
  Used when the user wants to send the text in the input field.
- ImeAction.Go
  Used when the user wants to navigate to the target of the text in the input.

Icons can make the text field more visually appealing and provide additional information about the text
field. Icons can be used to convey information about the purpose of the text field, such as what type of
data is expected or what kind of input is required.

There are many assertions in the JUnit library.

Instrumentation tests test an actual instance of the app and its UI, so the UI content must be set, similar
to how the content is set in the onCreate() method of the MainActivity.kt file when you wrote the code for
the Tip Time app. You need to do this before you write all instrumentation tests for apps built with Compose.


 */