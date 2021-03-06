package org.blockstack.android.sdk

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.blockstack.android.sdk.model.BlockstackConfig
import org.blockstack.android.sdk.model.toBlockstackConfig
import org.blockstack.android.sdk.test.TestActivity
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

class BlockstackSessionLoginTest() {
    private val TIMEOUT = 3000L

    @get:Rule
    val rule = ActivityTestRule(TestActivity::class.java)

    private lateinit var session: BlockstackSession
    private lateinit var sessionStore: SessionStore
    private lateinit var config: BlockstackConfig

    @Before
    fun setup() {
        config = "https://flamboyant-darwin-d11c17.netlify.com".toBlockstackConfig(emptyArray())
        sessionStore = sessionStoreforIntegrationTests(rule)
        session = BlockstackSession(rule.activity,
                config,
                sessionStore = sessionStore,
                executor = IntegrationTestExecutor(rule))
    }

    @After
    fun teardown() {
        session.release()
    }


    @Test
    fun redirectToSignInRespectsBetaModeFlag() {
        val betaSession = BlockstackSession(rule.activity,
                config,
                sessionStore = sessionStore,
                executor = IntegrationTestExecutor(rule),
                betaMode = true)

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        betaSession.redirectUserToSignIn {}

        val pattern = Pattern.compile("ACCEPT & CONTINUE|NO THANKS|https://beta.browser.blockstack.org")
        device.wait(Until.hasObject(By.text(pattern)), TIMEOUT)
        val tandcButton = device.findObject(UiSelector().text("ACCEPT & CONTINUE"))
        if (tandcButton.exists()) {
            tandcButton.click()
        }
        device.wait(Until.hasObject(By.text(pattern)), TIMEOUT)
        val accountButton = device.findObject(UiSelector().text("NO THANKS"))
        if (accountButton.exists()) {
            accountButton.click()
        }
        device.wait(Until.hasObject(By.text(pattern)), TIMEOUT)
        val betaBrowserLabel = device.findObject(UiSelector().text("https://beta.browser.blockstack.org"))
        assertThat(betaBrowserLabel.exists(), `is`(true))
        device.pressBack()
    }
}
