package com.financialapp.finances.domain.model.transaction;

import com.financialapp.finances.domain.exception.InvalidIdentifierException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorPageTest {

    @Test
    void encodesAndDecodesCursor() {
        LocalDate date = LocalDate.of(2026, 7, 15);
        Long id = 42L;

        String encoded = CursorPage.encode(date, id);
        CursorPage page = new CursorPage(encoded, 20);

        assertThat(page.decodedDate()).isEqualTo(date);
        assertThat(page.decodedId()).isEqualTo(id);
        assertThat(page.size()).isEqualTo(20);
    }

    @Test
    void handlesNullCursorAfter() {
        CursorPage page = new CursorPage(null, 50);

        assertThat(page.cursorAfter()).isNull();
        assertThat(page.decodedDate()).isNull();
        assertThat(page.decodedId()).isNull();
    }

    @Test
    void clampsSizeBounds() {
        assertThat(new CursorPage(null, 0).size()).isEqualTo(50);
        assertThat(new CursorPage(null, -5).size()).isEqualTo(50);
        assertThat(new CursorPage(null, 300).size()).isEqualTo(200);
        assertThat(new CursorPage(null, 15).size()).isEqualTo(15);
    }

    @Test
    void throwsOnMalformedCursor() {
        CursorPage page = new CursorPage("invalid-base64!!!", 50);
        assertThatThrownBy(page::decodedDate).isInstanceOf(InvalidIdentifierException.class);

        CursorPage pageBadFormat = new CursorPage("MjAyNi0wNy0xNQ", 50); // missing pipe and id
        assertThatThrownBy(pageBadFormat::decodedDate).isInstanceOf(InvalidIdentifierException.class);
    }

    @Test
    void theOffsetComponentIsARowOffsetAndSurvivesReconstruction() {
        CursorPage page = CursorPage.ofPage(null, 20, 2);
        assertThat(page.offset()).isEqualTo(40);

        CursorPage rebuilt = new CursorPage(page.cursorAfter(), page.size(), page.offset());
        assertThat(rebuilt.offset()).isEqualTo(40);
    }

    @Test
    void negativePagesAndOffsetsClampToZero() {
        assertThat(CursorPage.ofPage(null, 20, 0).offset()).isZero();
        assertThat(CursorPage.ofPage(null, 20, -3).offset()).isZero();
        assertThat(new CursorPage(null, 20, -7).offset()).isZero();
        assertThat(new CursorPage(null, 20).offset()).isNull();
    }

    @Test
    void aCursorWinsOverAPageNumber() {
        CursorPage page = CursorPage.ofPage(CursorPage.encode(LocalDate.of(2026, 6, 1), 7L), 20, 3);
        assertThat(page.offset()).isNull();
    }
}
