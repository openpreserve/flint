package uk.bl.dpt.qa.flint.formats;


import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import uk.bl.dpt.qa.flint.formats.MobiBook.MobiHeader;
import uk.bl.dpt.qa.flint.formats.MobiBook.PalmDatabaseHeader;


public class MobiBookTest {
    
    private MobiBook mobiBook = new MobiBook(MobiBookTest.class.getResourceAsStream("/mobisamples/lorem-ipsum.mobi"));

    @Test
    public void testMobiBook() {        
        assertTrue("Should be a valid Palm Database File", mobiBook.isValid());
        assertTrue("Should be a mobi book", mobiBook.isMobiFormat());
    }
    
    @Test 
    public void testPalmDocHeader() {
        PalmDatabaseHeader header = mobiBook.getPalmDatabaseHeader();
        
        assertEquals("BOOK", header.getType());
        assertEquals("MOBI", header.getCreator());
        assertEquals(7, header.getNumberOfRecords());
    }

    @Test
    public void testMobiHeader() {
        MobiHeader header = mobiBook.getMobiHeader();
        
        assertFalse(header.hasDRM());
        assertEquals(2, header.getCompression());
        assertEquals(0, header.getEncryptionType());
        assertEquals(2, header.getMobiType());
        assertEquals(0xFFFFFFFF, header.getDRMOffset());
        assertEquals(0, header.getDRMCount());
        assertEquals(" ", header.getFullName());
    }
    
    @Test
    public void testNoDRMMobiHeader() {
        MobiBook mobiBook = new MobiBook(MobiBookTest.class.getResourceAsStream("/mobisamples/lorem-ipsum.azw3"));
        MobiHeader header = mobiBook.getMobiHeader();
        
        assertFalse(header.hasDRM());
        assertEquals(2, header.getCompression());
        assertEquals(0, header.getEncryptionType());
        assertEquals(2, header.getMobiType());
        assertEquals(0xFFFFFFFF, header.getDRMOffset());
        assertEquals(0, header.getDRMCount());
        assertEquals("Unknown", header.getFullName());
    }
    
    /*
     * test ignored because we don't have permission to distribute the test file. 
     */
    @Test @Ignore
    public void testDRMMobiHeader() {
        MobiBook mobiBook = new MobiBook(MobiBookTest.class.getResourceAsStream("/mobisamples/B00968FPDE_EBOK.prc"));
        MobiHeader header = mobiBook.getMobiHeader();
        
        assertTrue(header.hasDRM());
        assertEquals(17480, header.getCompression());
        assertEquals(2, header.getEncryptionType());
        assertEquals(2, header.getMobiType());
        assertEquals(1320, header.getDRMOffset());
        assertEquals(1, header.getDRMCount());
        assertEquals("The Definitive Guide to Customer Relationship Management (Collection)", header.getFullName());
    }
    

}
