package no.uio.ifi.in2000.danishah.figmatesting.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpeciesMapperTest {

    @Test
    fun getIdCorrectForSpecies() {
        assertEquals(0f, SpeciesMapper.getId("torsk"), 0.0f)
        assertEquals(1f, SpeciesMapper.getId("makrell"), 0.0f)
        assertEquals(6f, SpeciesMapper.getId("gjedde"), 0.0f)
    }

    @Test
    fun getIdCaseInsensitivity() {
        assertEquals(0f, SpeciesMapper.getId("TORSK"), 0.0f)
        assertEquals(0f, SpeciesMapper.getId("Torsk"), 0.0f)
        assertEquals(0f, SpeciesMapper.getId("torsk"), 0.0f)
    }

    @Test
    fun getIdWhitespace() {
        assertEquals(0f, SpeciesMapper.getId(" torsk"), 0.0f)
        assertEquals(0f, SpeciesMapper.getId("torsk "), 0.0f)
        assertEquals(0f, SpeciesMapper.getId(" torsk "), 0.0f)
    }

    @Test
    fun getIdExpectedValueForUnknwnSpecies() {
        assertEquals(-1f, SpeciesMapper.getId("ukjent_fisk"), 0.0f)
        assertEquals(-1f, SpeciesMapper.getId(""), 0.0f)
    }

    @Test
    fun getNameCorrectNameKnownId() {
        assertEquals("torsk", SpeciesMapper.getName(0))
        assertEquals("makrell", SpeciesMapper.getName(1))
        assertEquals("gjedde", SpeciesMapper.getName(6))
    }

    @Test
    fun getNameNullforUnknownId() {
        assertNull(SpeciesMapper.getName(-1))
        assertNull(SpeciesMapper.getName(100))
    }
} 