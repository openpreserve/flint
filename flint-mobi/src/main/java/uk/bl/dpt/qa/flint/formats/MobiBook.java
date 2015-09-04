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


public class MobiBook {
    
    private static Logger log = LoggerFactory.getLogger(MobiBook.class);
    
    private boolean valid = true;
    
    private PalmDatabaseHeader header = null;
    
    private List<Record> records = new ArrayList<Record>();

    public MobiBook(File file) throws FileNotFoundException {
        this(new FileInputStream(file));   
    }
        
    public MobiBook(InputStream in) {
        DataInputStream is = null;
        try {
            is = new DataInputStream(in);
            
            // read the header
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
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isMobiFormat() {
        return header != null && header.getType().equalsIgnoreCase("BOOK") && header.getCreator().equalsIgnoreCase("MOBI");
    }
    
    public PalmDatabaseHeader getPalmDatabaseHeader() {
        return header;
    }
    
    public MobiHeader getMobiHeader() {
        if (!records.isEmpty()) {
            Record record = records.get(0);
            
            return record instanceof MobiHeader ? (MobiHeader) record : null;
        }
        
        return null;
    }
    
    private Record createRecord(DataInputStream is, int position) throws IOException {
        if (position == 0) {
            return new MobiHeader(is, position);
        } else {
            return new Record(is, position);
        }
    }
    
    
    class PalmDatabaseHeader {
        
        private ByteBuffer palmDocHeader = null;
        
        public PalmDatabaseHeader(DataInputStream is) throws IOException {
            byte[] buf = new byte[78];
            is.readFully(buf);
            
            palmDocHeader = ByteBuffer.wrap(buf);
            palmDocHeader.order(ByteOrder.BIG_ENDIAN);
            palmDocHeader.mark();
        }
        
        public String getType() {
            byte[] buf = new byte[4];
            
            palmDocHeader.reset().position(60);
            palmDocHeader.get(buf).reset();
            
           return new String(buf, Charset.forName("ASCII"));
        }
        
        public String getCreator() {
            byte[] buf = new byte[4];
            
            palmDocHeader.reset().position(64);
            palmDocHeader.get(buf).reset();
            
           return new String(buf, Charset.forName("ASCII"));
        }
        
        public short getNumberOfRecords() {
            return palmDocHeader.getShort(76);
        }
    }
    
    class MobiHeader extends Record {

        public MobiHeader(DataInputStream is, int position) throws IOException {
            super(is, position);
        }
        
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
         * Encryption Type: 0 == no encryption, 1 = Old Mobipocket Encryption, 2 = Mobipocket Encryption
         */
        public short getEncryptionType() {
            return record.getShort(12);
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
            return record.getInt(24);
        }
        
        /**
         * Offset to DRM key info in DRMed files. 0xFFFFFFFF if no DRM 
         */
        public int getDRMOffset() {
            return record.getInt(168);
        }
        
        /**
         * Number of entries in DRM info. 0xFFFFFFFF if no DRM 
         */
        public int getDRMCount() {
            return record.getInt(172);
        }
        
        /**
         * Number of bytes in DRM info.
         */
        public int getDRMSize() {
            return record.getInt(176);
        }
        
        /**
         * Some flags concerning the DRM info. 
         */
        public int getDRMFlags() {
            return record.getInt(180);
        }


        /**
         * Close the record and verify that it is a mobi header.
         */
        @Override
        void close() throws IOException {
            super.close();
            
            byte[] buf = new byte[4];
            
            record.reset().position(16);
            record.get(buf).reset();
            
            if (!new String(buf, Charset.forName("ASCII")).equalsIgnoreCase("MOBI"))
                throw new IOException("Not a mobi header");
        }
        
        
    }
    
    /**
     * A container for a generic Palm Database Record
     */
    class Record {
        
        protected ByteBuffer record = null;
        
        private int recordDataOffset;
        
        private int position;
        
        public Record(DataInputStream is, int position) throws IOException {
            byte[] buf = new byte[8];
            is.readFully(buf);
            
            ByteBuffer palmDirectoryEntry = ByteBuffer.wrap(buf);
            palmDirectoryEntry.order(ByteOrder.BIG_ENDIAN);
            palmDirectoryEntry.mark();
            
            this.position = position;
            recordDataOffset = palmDirectoryEntry.getInt(0);
        }

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
