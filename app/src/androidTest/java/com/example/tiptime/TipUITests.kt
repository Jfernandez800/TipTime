package com.example.tiptime

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import com.example.tiptime.ui.theme.TipTimeTheme
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import java.text.NumberFormat

class TipUITests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calculate_20_percent_tip() {
        //This sets the UI content of the composeTestRule.
        composeTestRule.setContent {
            //call the TipTimeLayout() function.
            TipTimeTheme {
                TipTimeLayout()
            }
        }
        //Use the onNodeWithText() method to access the TextField composable for the bill amount.
        composeTestRule.onNodeWithText("Bill Amount")
            //Populate the TextField for the bill amount with a 10 value
            .performTextInput("10")
        composeTestRule.onNodeWithText("Tip Percentage")
            //Populate the OutlinedTextField for the bill amount with a 20 value
            .performTextInput("20")
        //Make an assertion that a node with that text exists
        val expectedTip = NumberFormat.getCurrencyInstance().format(2)
        composeTestRule.onNodeWithText("Tip Amount: $expectedTip").assertExists(
            "No node with this text was found."
        )
    }
}

