package com.application.personal_budget_app.domain.entry

import com.application.personal_budget_app.domain.model.Money
import org.junit.Assert.*
import org.junit.Test

class AmountInputTest {
    private fun type(vararg keys: KeypadKey) = keys.fold(AmountInput()) { acc, k -> acc.press(k) }
    private fun d(v: Int) = KeypadKey.Digit(v)

    @Test fun `typing 8 comma 4 gives 8,40 euros`() {
        val input = type(d(8), KeypadKey.Comma, d(4))
        assertEquals("8,4", input.display)
        assertEquals(Money(840), input.money)
    }

    @Test fun `third decimal is ignored`() =
        assertEquals(Money(1234), type(d(1), d(2), KeypadKey.Comma, d(3), d(4), d(5)).money)

    @Test fun `comma first gives 0 comma`() = assertEquals("0,", type(KeypadKey.Comma).raw)

    @Test fun `second comma is ignored`() = assertEquals("1,", type(d(1), KeypadKey.Comma, KeypadKey.Comma).raw)

    @Test fun `leading zero is replaced`() = assertEquals("5", type(d(0), d(5)).raw)

    @Test fun `delete removes last character`() = assertEquals("1", type(d(1), d(2), KeypadKey.Delete).raw)

    @Test fun `integer part is limited to 6 digits`() =
        assertEquals("999999", type(*Array(7) { d(9) }).raw)

    @Test fun `display groups thousands`() =
        assertEquals("1\u00A0250", type(d(1), d(2), d(5), d(0)).display)

    @Test fun `empty or zero amount is not valid`() {
        assertFalse(AmountInput().isValid)
        assertFalse(type(d(0), KeypadKey.Comma, d(0)).isValid)
    }
}