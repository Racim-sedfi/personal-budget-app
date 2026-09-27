package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTest {
    private val sixty = Money.euros(60)

    @Test fun `nothing spent gives an empty bar`() = assertEquals(0f, progressFraction(Money.ZERO, sixty), 0f)

    @Test fun `half spent fills half the bar`() = assertEquals(0.5f, progressFraction(Money.euros(30), sixty), 0.001f)

    @Test fun `exactly at cap fills the bar`() = assertEquals(1f, progressFraction(sixty, sixty), 0f)

    @Test fun `over cap stays full and does not grow`() = assertEquals(1f, progressFraction(Money.euros(95), sixty), 0f)

    @Test fun `more refunds than expenses gives an empty bar`() = assertEquals(0f, progressFraction(Money.euros(-5), sixty), 0f)

    @Test fun `spending without plan fills the bar`() = assertEquals(1f, progressFraction(Money.euros(10), Money.ZERO), 0f)

    @Test fun `percent can go above 100`() = assertEquals(158, usedPercent(Money.euros(95), sixty))

    @Test fun `percent is rounded`() = assertEquals(72, usedPercent(Money.euros(64, 50), Money.euros(90)))
}