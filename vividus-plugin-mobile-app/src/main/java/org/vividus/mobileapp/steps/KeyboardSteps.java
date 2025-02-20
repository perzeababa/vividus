/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.mobileapp.steps;

import static java.util.Map.entry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.mobileapp.action.KeyboardActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.PressesKey;

@TakeScreenshotOnFailure
public class KeyboardSteps
{
    private static final Map<String, String> ANDROID_KEYS = Map.ofEntries(
        entry(" ", AndroidKey.SPACE.toString()),
        entry("0", AndroidKey.DIGIT_0.toString()),
        entry("1", AndroidKey.DIGIT_1.toString()),
        entry("2", AndroidKey.DIGIT_2.toString()),
        entry("3", AndroidKey.DIGIT_3.toString()),
        entry("4", AndroidKey.DIGIT_4.toString()),
        entry("5", AndroidKey.DIGIT_5.toString()),
        entry("6", AndroidKey.DIGIT_6.toString()),
        entry("7", AndroidKey.DIGIT_7.toString()),
        entry("8", AndroidKey.DIGIT_8.toString()),
        entry("9", AndroidKey.DIGIT_9.toString()));

    private static final String ELEMENT_TO_TYPE_TEXT = "The element to type text";
    private static final String ELEMENT_TO_CLEAR = "The element to clear";

    private final KeyboardActions keyboardActions;
    private final IBaseValidations baseValidations;
    private final GenericWebDriverManager genericWebDriverManager;
    private final JavascriptActions javascriptActions;
    private final IWebDriverProvider webDriverProvider;

    private Duration longPressDuration;

    public KeyboardSteps(KeyboardActions keyboardActions, IBaseValidations baseValidations,
            GenericWebDriverManager genericWebDriverManager,
            JavascriptActions javascriptActions, IWebDriverProvider webDriverProvider)
    {
        this.keyboardActions = keyboardActions;
        this.baseValidations = baseValidations;
        this.genericWebDriverManager = genericWebDriverManager;
        this.javascriptActions = javascriptActions;
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Type <b>text</b> into the <b>element</b>
     * @param text text to type into the element
     * @param locator locator to find an element
     */
    @When("I type `$text` in field located `$locator` and keep keyboard opened")
    public void typeTextInFieldAndKeepKeyboard(String text, Locator locator)
    {
        baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)
                .ifPresent(e -> keyboardActions.typeText(e, text));
    }

    /**
     * Type <b>text</b> into the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>type text into the element</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param text text to type into the element
     * @param locator locator to find an element
     */
    @When("I type `$text` in field located `$locator`")
    public void typeTextInField(String text, Locator locator)
    {
        baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)
                .ifPresent(e -> {
                    keyboardActions.typeText(e, text);
                    keyboardActions.hideKeyboard(e);
                });
    }

    /**
     * Clear a field located by the <b>locator</b>
     * @param locator locator to find a field
     */
    @When("I clear field located `$locator` and keep keyboard open")
    public void clearTextInFieldAndKeepKeyboard(Locator locator)
    {
        baseValidations.assertElementExists(ELEMENT_TO_CLEAR, locator).ifPresent(keyboardActions::clearText);
    }

    /**
     * Clear a field located by the <b>locator</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>clear the field</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param locator locator to find a field
     */
    @When("I clear field located `$locator`")
    public void clearTextInField(Locator locator)
    {
        baseValidations.assertElementExists(ELEMENT_TO_CLEAR, locator)
                .ifPresent(e -> {
                    keyboardActions.clearText(e);
                    keyboardActions.hideKeyboard(e);
                });
    }

    /**
     * Presses the key
     * <br>
     * See
     * <a href="https://appium.github.io/appium-xcuitest-driver/5.6/execute-methods/#mobile-pressbutton">iOS keys</a>
     * and
     * <a href="https://appium.github.io/java-client/io/appium/java_client/android/nativekey/AndroidKey.html">
     * Android keys</a> for available values
     * <br>
     * Example:
     * <br>
     * <code>
     * When I press $key key
     * </code>
     *
     * @param key the key to press
     */
    @When("I press $key key")
    public void pressKey(String key)
    {
        pressKeys(List.of(key));
    }

    /**
     * Performs long press of the key
     * <br>
     * See
     * <a href="https://appium.github.io/appium-xcuitest-driver/5.6/execute-methods/#mobile-pressbutton">iOS keys</a>
     * and
     * <a href="https://appium.github.io/java-client/io/appium/java_client/android/nativekey/AndroidKey.html">
     * Android keys</a> for available values
     * <br>
     * Example:
     * <br>
     * <code>
     * When I long press RETURN key
     * </code>
     *
     * @param key the key to press
     */
    @When("I long press $key key")
    public void longPressKey(String key)
    {
        if (genericWebDriverManager.isAndroid())
        {
            pressOnAndroid(p -> p::longPressKey, List.of(key));
        }
        else if (genericWebDriverManager.isIOS() || genericWebDriverManager.isTvOS())
        {
            pressButton(key, Optional.of(longPressDuration));
        }
    }

    /**
     * Presses the keys
     * <br>
     * See
     * <a href="https://appium.github.io/appium-xcuitest-driver/5.6/execute-methods/#mobile-pressbutton">iOS keys</a>
     * and
     * <a href="https://appium.github.io/java-client/io/appium/java_client/android/nativekey/AndroidKey.html">
     * Android keys</a> for available values
     * <br>
     * Example:
     * <br>
     * <code>
     * When I press keys:
     * <br>
     * |key |
     * <br>
     * |Home|
     * </code>
     *
     * @param keys the keys to press
     */
    @When("I press keys:$keys")
    public void pressKeys(ExamplesTable keys)
    {
        pressKeys(keys.getColumn("key"));
    }

    /**
     * Types the text into the focused field
     * @param text the text to type
     */
    @When("I type text `$text`")
    public void typeKeys(String text)
    {
        UnaryOperator<String> mapper = genericWebDriverManager.isAndroid() ? c -> ANDROID_KEYS.getOrDefault(c, c)
                : UnaryOperator.identity();
        List<String> toType = text.chars()
                                  .boxed()
                                  .map(Character::toString)
                                  .map(mapper)
                                  .toList();
        pressKeys(toType);
    }

    private void pressKeys(List<String> keys)
    {
        if (genericWebDriverManager.isIOS() || genericWebDriverManager.isTvOS())
        {
            keys.forEach(key -> pressButton(key, Optional.empty()));
        }
        else
        {
            pressOnAndroid(p -> p::pressKey, keys);
        }
    }

    private Object pressButton(String key, Optional<Duration> pressDuration)
    {
        Map<String, Object> args = new HashMap<>(2);
        args.put("name", key);
        pressDuration.ifPresent(d -> args.put("durationSeconds", longPressDuration.toSeconds()));
        return javascriptActions.executeScript("mobile: pressButton", args);
    }

    private void pressOnAndroid(Function<PressesKey, Consumer<KeyEvent>> presser, List<String> keys)
    {
        PressesKey pressesKey = webDriverProvider.getUnwrapped(PressesKey.class);
        Consumer<KeyEvent> keyPresser = presser.apply(pressesKey);
        keys.stream()
            .map(key ->
            {
                AndroidKey androidKey = EnumUtils.getEnumIgnoreCase(AndroidKey.class, key);
                Validate.isTrue(androidKey != null, "Unsupported Android key: %s", key);
                return androidKey;
            })
            .map(KeyEvent::new)
            .forEach(keyPresser::accept);
    }

    public void setLongPressDuration(Duration longPressDuration)
    {
        this.longPressDuration = longPressDuration;
    }
}
