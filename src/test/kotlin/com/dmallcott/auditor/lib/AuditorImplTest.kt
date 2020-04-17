package com.dmallcott.auditor.lib

import com.dmallcott.auditor.Quote
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class AuditorImplTest {

    val parser = mockk<Parser>()
    val repository = mockk<Repository>()
    val underTest = AuditorImpl(parser, repository)

    @Test
    internal fun name() {
        val quote = getQuote()

        every { repository.test(quote.id) } returns AuditLog<Any>(quote.id.id, quote, emptyList())

        underTest.test(quote.id)

        verify { repository.test(quote.id) }
    }

    @Test
    internal fun dasdasdas() {
        val quote = getQuote()

        every { repository.find2(quote.id, Quote::class.java) } returns null
        every { repository.create2(quote.id, quote, Quote::class.java) } returns true

        underTest.log(quote.id, quote)

        verify { repository.create2(quote.id, quote, Quote::class.java) }
    }
}