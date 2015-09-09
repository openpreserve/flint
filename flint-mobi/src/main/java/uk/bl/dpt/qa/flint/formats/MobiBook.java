package uk.bl.dpt.qa.flint.formats;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple parser for MobiBook files to allow the extraction of metadata.
 * 
 * Based on http://wiki.mobileread.com/wiki/MOBI.
 */
public class MobiBook {
    
    private static Logger log = LoggerFactory.getLogger(MobiBook.class);
    
    private boolean valid = true;
    
    /** the Palm Database header */
    private PalmDatabaseHeader header = null;
    
    /** the list of records in the database */
    private List<Record> records = new ArrayList<Record>();

    /**
     * Constructor.
     * @param file  the mobi book file
     * @throws FileNotFoundException if the file was not found
     */
    public MobiBook(File file) throws FileNotFoundException {
        this(new FileInputStream(file));   
    }
    
    /**
     * Constructor.
     * @param in    the mobi book file as a stream
     */
    public MobiBook(InputStream in) {
        DataInputStream is = null;
        try {
            is = new DataInputStream(in);
            
            // read the database header
            header = new PalmDatabaseHeader(is);
            if (!isMobiFormat() || header.getNumberOfRecords() < 2) {
                valid = false;
                return;
            }
            
            // read the directory
            for (int i = 0; i < header.getNumberOfRecords(); i++) {
                records.add(createRecord(is, i));
            }
            
            Iterator<Record> it = records.iterator();
            Record currentEntry = it.next();
            Record nextEntry = it.hasNext() ? it.next() : null;
            
            // seek to the first record
            int pos = 78 + (8 * header.getNumberOfRecords());
            while (pos++ < currentEntry.getRecordDataOffset()) {
                is.readByte();
            }
            
            // read all the records
            try {
                while (true) {
                    byte b = is.readByte();
                    pos++;
                    
                    if (nextEntry != null && pos == nextEntry.getRecordDataOffset()) {
                        currentEntry.close();
                        currentEntry = nextEntry;
                        nextEntry = it.hasNext() ? it.next() : null;
                    }
                    
                    currentEntry.append(b);
                }
            } catch (EOFException e) {
               currentEntry.close(); 
            }

                
        } catch (FileNotFoundException e) {
            valid = false;
            e.printStackTrace();
            log.info("Unable to open file", e);
        } catch (IOException e) {
            valid = false;
            e.printStackTrace();
            log.info("Unable to read file", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                log.info("Unable to close input stream", e);
            }
        }
    }
    
    /**
     * True if the database could be read and it contains a mobi book.
     * @return
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * True if this database is a mobi book.
     */
    public boolean isMobiFormat() {
        return header != null && header.getType().equalsIgnoreCase("BOOK") && header.getCreator().equalsIgnoreCase("MOBI");
    }
    
    public PalmDatabaseHeader getPalmDatabaseHeader() {
        return header;
    }
    
    /**
     * Get the Mobi header from position 0 in the database.
     */
    public MobiHeader getMobiHeader() {
        if (!records.isEmpty()) {
            Record record = records.get(0);
            
            return record instanceof MobiHeader ? (MobiHeader) record : null;
        }
        
        return null;
    }
    
    /**
     * Create a new Palm Database record.
     * @param is            the Palm Database contents as a stream
     * @param position      the position of the record in the database 
     * @return either a specific type of record (ie: MobiHeader) or a generic record.
     * @throws IOException  if an error occurs reading the database
     */
    private Record createRecord(DataInputStream is, int position) throws IOException {
        if (position == 0) {
            return new MobiHeader(is, position);
        } else {
            return new Record(is, position);
        }
    }
    
    /**
     * The Palm Database header.
     * See http://wiki.mobileread.com/wiki/PDB#Palm_Database_Format
     */
    public class PalmDatabaseHeader {
        
        private ByteBuffer palmDocHeader = null;
        
        /**
         * Constructor.
         * @param is    the input file as a stream
         * @throws IOException if an error occurred reading the 78 bytes of the header
         */
        public PalmDatabaseHeader(DataInputStream is) throws IOException {
            byte[] buf = new byte[78];
            is.readFully(buf);
            
            palmDocHeader = ByteBuffer.wrap(buf);
            palmDocHeader.order(ByteOrder.BIG_ENDIAN);
            palmDocHeader.mark();
        }
        
        /**
         * The type of database.
         */
        public String getType() {
            byte[] buf = new byte[4];
            
            palmDocHeader.reset().position(60);
            palmDocHeader.get(buf).reset();
            
           return new String(buf, Charset.forName("ASCII"));
        }
        
        /**
         * The program that uses this database
         */
        public String getCreator() {
            byte[] buf = new byte[4];
            
            palmDocHeader.reset().position(64);
            palmDocHeader.get(buf).reset();
            
           return new String(buf, Charset.forName("ASCII"));
        }
        
        /**
         * The number of records in the database
         */
        public short getNumberOfRecords() {
            return palmDocHeader.getShort(76);
        }
    }
    
    public class MobiHeader extends Record {
        
        private Charset textEncoding = Charset.forName("UTF-8");
        
        private int headerLength = 24;

        /**
         * Constructor.
         * @param is            the palm database file as a stream
         * @param position      the position of the record in the database
         * @throws IOException  if the header could not be read
         */
        public MobiHeader(DataInputStream is, int position) throws IOException {
            super(is, position);
        }
        
        /**
         * True if the encryption type is set or the database has a DRM offset set.
         */
        public boolean hasDRM() {
            return getEncryptionType() > 0 || getDRMOffset() < 0xFFFFFFFF;
        }
        
        /**
         * Compression: 1 == no compression, 2 = PalmDOC compression, 17480 = HUFF/CDIC compression 
         */
        public short getCompression() {
            return record.getShort(0);
        }
        
        /**
         * Uncompressed length of the entire text of the book.
         */
        public int getTextLength() {
            return record.getInt(4);
        }
        
        /**
         * Number of PDB records used for the text of the book. 
         */
        public short getRecordCount() {
            return record.getShort(8);
        }
        
        /**
         * Maximum size of each record containing text, always 4096
         */
        public short getRecordSize() {
            return record.getShort(10);
        }
        
        /**
         * Encryption Type: 0 == no encryption, 1 = Old Mobipocket Encryption, 2 = Mobipocket Encryption
         */
        public short getEncryptionType() {
            return record.getShort(12);
        }
        
        /**
         * The characters M O B I 
         */
        public String getIdentifier() {
            return getString(16, 4);
        }
        
        /**
         * Length of the mobi header.
         */
        public int getHeaderLength() {
            return record.getInt(20);
        }
        
        /**
         * The kind of Mobipocket file this is: 2 Mobipocket Book, 3 PalmDoc Book, 4 Audio, 232 mobipocket? generated by kindlegen1.2,
         * 248 KF8: generated by kindlegen2, 257 News, 258 News_Feed, 259 News_Magazine, 513 PICS, 514 WORD, 515 XLS, 516 PPT, 517 TEXT, 518 HTML 
         */
        public int getMobiType() {
            return getInt(24);
        }
        
        /**
         * 1252 = CP1252 (WinLatin1); 65001 = UTF-8 
         */
        public int getTextEncoding() {
            return getInt(28);
        }
        
        /**
         * Some kind of unique ID number (random?)  
         */
        public int getUniqueID() {
            return getInt(32);
        }
        
        /**
         * Version of the Mobipocket format used in this file.  
         */
        public int getFileVersion() {
            return getInt(36);
        }
    
        /**
         * Offset in record 0 (not from start of file) of the full name of the book   
         */
        public int getFullNameOffset() {
            return getInt(84);
        }
        
        /**
         * Length in bytes of the full name of the book    
         */
        public int getFullNameLength() {
            return getInt(88);
        }
        
        /**
         * The full name of the book    
         */
        public String getFullName() {
            return getString(getFullNameOffset(), getFullNameLength());
        }
        
        /**
         * Offset to DRM key info in DRMed files. 0xFFFFFFFF if no DRM 
         */
        public int getDRMOffset() {
            return getInt(168);
        }
        
        /**
         * Number of entries in DRM info. 0xFFFFFFFF if no DRM 
         */
        public int getDRMCount() {
            return getInt(172);
        }
        
        /**
         * Number of bytes in DRM info.
         */
        public int getDRMSize() {
            return getInt(176);
        }
        
        /**
         * Some flags concerning the DRM info. 
         */
        public int getDRMFlags() {
            return getInt(180);
        }


        /**
         * Close the record and verify that it is a mobi header.
         */
        @Override
        void close() throws IOException {
            super.close();
            
            String identifier = getIdentifier();           
            if (!identifier.equalsIgnoreCase("MOBI"))
                throw new IOException("Not a mobi header");
            
            headerLength = record.capacity();
            
            int encoding = getTextEncoding();
            if (encoding == 1252) {
                textEncoding = Charset.forName("ASCII");
            }
        }
        
        /**
         * Read a short from the record
         * @param pos   the position of the short in the record
         */
        private short getShort(int pos) {
            if (headerLength < pos + 2) throw new ArrayIndexOutOfBoundsException(pos);
            return record.getShort(pos);
        }
        
        /**
         * Read an int from the record
         * @param pos   the position of the int in the record
         */
        private int getInt(int pos) {
            if (headerLength < pos + 4) throw new ArrayIndexOutOfBoundsException(pos);
            return record.getInt(pos);
        }
        
        /**
         * Read a string from the record
         * @param pos   the start position of the string in the record
         * @param size  the length of the string
         * @return the string parsed using the text encoding.
         */
        private String getString(int pos, int size) {
            if (headerLength < pos + size) throw new ArrayIndexOutOfBoundsException(pos);
            
            byte[] buf = new byte[size];
            
            record.reset().position(pos);
            record.get(buf).reset();
            
           return new String(buf, textEncoding);
        }
    }
    
    /**
     * A container for a generic Palm Database Record
     */
    public class Record {
        
        /** the contents of the record */
        protected ByteBuffer record = null;
        
        /** the offset of the record data in the database */
        private int recordDataOffset;
        
        /** the position of the record in the database */
        private int position;
        
        /**
         * Constructor.
         * @param is            the palm database file as a stream
         * @param position      the position of the record in the database
         * @throws IOException  if an error occurs while reading the record
         */
        public Record(DataInputStream is, int position) throws IOException {
            byte[] buf = new byte[8];
            is.readFully(buf);
            
            ByteBuffer palmDirectoryEntry = ByteBuffer.wrap(buf);
            palmDirectoryEntry.order(ByteOrder.BIG_ENDIAN);
            palmDirectoryEntry.mark();
            
            this.position = position;
            recordDataOffset = palmDirectoryEntry.getInt(0);
        }

        /**
         * The offset of the record data in the database
         */
        public int getRecordDataOffset() {
            return recordDataOffset;
        }
        
        /**
         * The position of this record in the database
         */
        public int getPosition() {
            return position;
        }
        
        /**
         * The raw data of the record
         */
        public byte[] getData() {
            return record.array();
        }
        
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        /**
         * Append a byte to this record
         */
        void append(byte b) {
            baos.write(b);
        }
        
        /**
         * Close 
         */
        void close() throws IOException {
            baos.close();

            record = ByteBuffer.wrap(baos.toByteArray());
            record.order(ByteOrder.BIG_ENDIAN);
            record.mark();
        }
        
    }

}
