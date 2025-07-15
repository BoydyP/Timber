package com.android.timberworkoutlogs.settings

//import android.content.Context
//import androidx.compose.ui.semantics.Role
//import androidx.compose.ui.semantics.SemanticsProperties
//import androidx.compose.ui.test.SemanticsMatcher
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.assertIsOn
//import androidx.compose.ui.test.hasAnyAncestor
//import androidx.compose.ui.test.hasText
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onNodeWithContentDescription
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.preferencesDataStore
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.android.timberworkoutlogs.MainActivity
//import com.android.timberworkoutlogs.models.WeightUnit
//import com.android.timberworkoutlogs.models.toStringResource
//import kotlinx.coroutines.runBlocking
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//private const val TEST_SETTINGS = "test_settings_preferences"
//
//private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(
//    name = TEST_SETTINGS
//)
//
//@RunWith(AndroidJUnit4::class)
//class SettingsInteractionTest {
//
//    @get:Rule
//    val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//
//    val context = InstrumentationRegistry.getInstrumentation().targetContext
//    val kgLabel = context.getString(WeightUnit.KG.toStringResource()) // Gets "KG"
//    val lbLabel = context.getString(WeightUnit.LB.toStringResource()) // Gets "LB"
//
//    private lateinit var dataStore: DataStore<Preferences>
//
//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        dataStore = context.testDataStore
//        runBlocking {
//            dataStore.edit { preferences ->
//                preferences.clear()
//            }
//        }
//    }
//
//    @Test
//    fun weightUnitSetting_whenChangedToLB_isReflectedInNewWorkout() {
//        // 1. Navigate to Settings
//        composeTestRule.onNodeWithText("Settings").performClick()
//
//        // 2. Find the Switch associated with "Weight Unit" and click it
//        composeTestRule.onNodeWithText(kgLabel).assertIsDisplayed()
//
//        composeTestRule.onNode(
//            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and hasAnyAncestor(hasText(lbLabel))
//        ).performClick()
//
//        // 3. Navigate to the Workout screen
//        composeTestRule.onNodeWithText("Workout").performClick()
//
//        // 4. Start a new workout by clicking the add button
//        composeTestRule.onNodeWithContentDescription("Add Exercise").performClick()
//
//        // 5. Assert that the UnitSwitch is now in the "on" state (representing LB)
//        composeTestRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertIsOn()
//    }
//}
