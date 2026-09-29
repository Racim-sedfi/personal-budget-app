package com.application.personal_budget_app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    private fun nb(s: String) = s.replace(' ', '\u00A0')

    @Test fun `formats with thousands separator and comma`() {
        assertEquals(nb("1 850,00 €"), Money.euros(1850).format())
        assertEquals(nb("557,10 €"), Money.euros(557, 10).format())
    }

    @Test fun `formats zero and small amounts`() {
        assertEquals(nb("0,00 €"), Money.ZERO.format())
        assertEquals(nb("0,05 €"), Money(5).format())
    }

    @Test fun `formats negative amounts with a real minus sign`() {
        assertEquals(nb("−150,00 €"), (-Money.euros(150)).format())
    }

    @Test fun `arithmetic is exact`() {
        val total = listOf(Money(10), Money(20)).sum()
        assertEquals(Money(30), total)
        assertEquals(Money.euros(399, 90), Money.euros(142, 30) + Money.euros(257, 60))
    }

    @Test fun `formats with the chosen currency symbol`() {
        assertEquals("12,50\u00A0CHF", Money.euros(12, 50).format(AppCurrency.CHF))
        assertEquals("1\u00A0650,00\u00A0DH", Money.euros(1650).format(AppCurrency.MAD))
        assertEquals("−3,00\u00A0£", (-Money.euros(3)).format(AppCurrency.GBP))
    }

    @Test fun `euro is the default currency`() =
        assertEquals("5,00\u00A0€", Money.euros(5).format())

    @Test fun `currency is found by its code`() {
        assertEquals(AppCurrency.DZD, AppCurrency.fromCode("DZD"))
        assertNull(AppCurrency.fromCode("XYZ"))
    }
}