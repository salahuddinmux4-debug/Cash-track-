package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        LoginScreen(
          onLoginSuccess = {},
          onLoginAttempt = { _, _ -> true }
        )
      }
    }

    composeTestRule.waitForIdle()
    val file = java.io.File("app/src/test/screenshots/greeting.png").let {
      if (it.parentFile?.exists() == true) it else java.io.File("src/test/screenshots/greeting.png")
    }
    composeTestRule.onRoot().captureRoboImage(filePath = file.path)
  }
}
