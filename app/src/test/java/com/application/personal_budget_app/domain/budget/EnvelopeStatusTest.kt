package com.application.personal_budget_app.domain.budgt

import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.envelopeStatus
import com.application.personal_budget_app.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class EnvelopeStatusTest {
    @Test fun `courses at 57 percent is normal`() =
        assertEquals(EnvelopeStatus.NORMAL, envelopeStatus(Money.euros(142, 30), Money.euros(250)))

    @Test fun `restaurants at 72 percent is near limit`() =
        assertEquals(
            EnvelopeStatus.NEAR_LIMIT,
            envelopeStatus(Money.euros(64, 50), Money.euros(90))
        )

    @Test fun `exactly 70 percent is near limit`() =
        assertEquals(EnvelopeStatus.NEAR_LIMIT, envelopeStatus(Money.euros(70), Money.euros(100)))

    @Test fun `exactly at cap is not over`() =
        assertEquals(EnvelopeStatus.NEAR_LIMIT, envelopeStatus(Money.euros(60), Money.euros(60)))

    @Test fun `loisirs above cap is over`() =
        assertEquals(EnvelopeStatus.OVER, envelopeStatus(Money.euros(95), Money.euros(60)))

    @Test fun `empty envelope is normal`() =
        assertEquals(EnvelopeStatus.NORMAL, envelopeStatus(Money.ZERO, Money.euros(60)))
}