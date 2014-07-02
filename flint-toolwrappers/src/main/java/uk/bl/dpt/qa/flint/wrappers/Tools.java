/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package uk.bl.dpt.qa.flint.wrappers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Tools class
 * @author wpalmer
 *
 */
public class Tools {

	private Tools() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Recover the value associated with a xpath expression
	 * @param pFile file
	 * @param pXPath XPath expression to evaluate
	 * @return value value associated with the xpath expression (or null if error)
	 */
	public static String getXpathVal(File pFile, String pXPath) {
		try {
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docB.parse(pFile);
			Node root = doc.getFirstChild();
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(pXPath, root);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Recover the value associated with a xpath expression
	 * @param pFileName xml file
	 * @param pXPath XPath expression to evaluate
	 * @return value value associated with the xpath expression (or null if error)
	 */
	public static String getXpathVal(InputStream pFileName, String pXPath) {
		try {
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docB.parse(pFileName);
			Node root = doc.getFirstChild();		
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(pXPath, root);
		} catch (ParserConfigurationException pce) {
		} catch (NumberFormatException e) {
		} catch (XPathExpressionException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * Copies an inputstream to a file
	 * @param pClass base class to use to find resource
	 * @param pResource resource to find
	 * @param pFile local file to write to
	 * @throws IOException if an error occurred
	 */
	public static void copyResourceToFile(Class<?> pClass, String pResource, String pFile) throws IOException {
		FileOutputStream out = new FileOutputStream(pFile);
		InputStream in = getResource(pClass, pResource);
		byte[] readBuffer = new byte[32768];
		int bytesRead = 0;
		while(in.available()>0) {
			bytesRead = in.read(readBuffer);
			out.write(readBuffer, 0, bytesRead);
		}
		in.close();
		out.close();
	}	
	
	/**
	 * Copies an inputstream to a file
	 * @param pBuffer buffer to read
	 * @param pFile local file to write to
	 * @throws IOException if an error occurred
	 */
	public static void copyBufferToFile(Reader pBuffer, File pFile) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(pFile));
		char[] readBuffer = new char[32768];
		int bytesRead = 0;
		while(pBuffer.ready()) {
			bytesRead = pBuffer.read(readBuffer);
			out.write(readBuffer, 0, bytesRead);
		}
		out.close();
	}
	
	/**
	 * Gets an InputStream for a resource from a jar
	 * @param pClass Class reference
	 * @param pRes resource path in jar
	 * @return InputStream for resource
	 */
	public static InputStream getResource(Class<?> pClass, String pRes) {
		return pClass.getClassLoader().getResourceAsStream(pRes);
	}
	
	/**
	 * Generates a checksum for a file 
	 * @param pType type of checksum to generate (MD5/SHA1 etc)
	 * @param pInFile file to checksum
	 * @return A String with the format MD5:XXXXXX or SHA1:XXXXXX
	 * @throws IOException file access error
	 */
	public static String generateChecksum(String pType, String pInFile) throws IOException {

		if(!new File(pInFile).exists()) throw new IOException("File not found: "+pInFile);
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(pType.toUpperCase());
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		
		FileInputStream input;
		try {
			input = new FileInputStream(pInFile);
			byte[] readBuffer = new byte[32768];
			int bytesRead = 0;
			while(input.available()>0) {
				bytesRead = input.read(readBuffer);
				md.update(readBuffer, 0, bytesRead);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String hash = "";
		for(byte b : md.digest()) hash+=String.format("%02x", b);
		return hash;
	}	
	
	/**
	 * Recursively delete a local directory
	 * @param pDir directory to delete
	 * @return success
	 */
	public static boolean deleteDirectory(File pDir) {
		boolean ret = true;
		for(File f:pDir.listFiles()) {
			if(f.isDirectory()) ret&=deleteDirectory(f);
			ret&=f.delete();
		}
		ret&=pDir.delete();
		return ret;
	}
	
	/**
	 * Creates a new temporary directory 
	 * @return File object for new directory
	 * @throws IOException file access error
	 */
	public static File newTempDir() {
		
		final String TMP_DIR = "/tmp/flint-hadoop/";
		File dir = new File(TMP_DIR); 
		dir.mkdirs();
		return dir;
		
	}
	
	/**
	 * Generates a checksum for a file 
	 * @param pInFile file to checksum
	 * @return A String with the format MD5:XXXXXX or SHA1:XXXXXX
	 * @throws IOException file access error
	 */
	public static String generateChecksum(String pInFile) throws IOException {

		String type = "MD5";
		return type+":"+generateChecksum(type, pInFile);
		
	}

	/**
	 * Convenience method to zip the generated files together (no compression)
	 * @param pSuccess whether workflow was successful or not
	 * @param pChecksums checksums for all files to be zipped
	 * @param pGeneratedFiles list of files to be zipped
	 * @param pZipFile output zip file
	 * @param pTempDir local temporary directory that contains files to zip 
	 * @throws IOException file access error
	 */
	public static void zipGeneratedFiles(boolean pSuccess, HashMap<String, String> pChecksums, 
			List<String> pGeneratedFiles, String pZipFile, String pTempDir) throws IOException {
		
		final int BUFSIZE = 32768;
		
		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(pZipFile));

		int compression = ZipEntry.STORED;
		
		//add an empty file indicating success or failure
		ZipEntry status;
		if(pSuccess) status = new ZipEntry("SUCCESS");
		else status = new ZipEntry("FAILURE");
		status.setSize(0);
		status.setTime(0);
		status.setMethod(compression);
		status.setCrc(0);
		zip.putNextEntry(status);
		zip.closeEntry();
		
		//generate a manifest file and write it to the zip as the first entry
		ZipEntry manifest = new ZipEntry("manifest-md5.txt");
		manifest.setTime(0);
		manifest.setMethod(compression);
		CRC32 crc = new CRC32();
		long size = 0;
		for(String file : pChecksums.keySet()) {
			//nasty hack
			if(file.endsWith(".report.xml")) continue;
			
			//String fn = new File(file).getName();
			//only add the file if it exists!
			if(new File(pTempDir+file).exists()) {
				String out = pChecksums.get(file).split(":")[1]+"  data/"+file+"\n";
				size+=out.getBytes().length;
				crc.update(out.getBytes());
			}
		}
		manifest.setCrc(crc.getValue());
		manifest.setSize(size);
		zip.putNextEntry(manifest);
		for(String file : pChecksums.keySet()) {
			//nasty hack
			if(file.endsWith(".report.xml")) continue;

			//String fn = new File(file).getName();
			
			//THIS MUST MATCH THE ABOVE STRING EXACTLY!
			//only add the file if it exists!
			if(new File(pTempDir+file).exists()) {
				String out = pChecksums.get(file).split(":")[1]+"  data/"+file+"\n";
				zip.write(out.getBytes());
			}
		}
		zip.closeEntry();
		
		//copy all the files in
		for(String file : pGeneratedFiles) {
			//add file to zip
			File input = new File(pTempDir+file);
			
			//file does not exist - obvious error condition but continue anyhow
			if(!input.exists()) continue;
			
			FileInputStream inData = new FileInputStream(input);
			ZipEntry entry;
			//hack to shorten report and log file names
			if(file.endsWith(".report.xml")) {
				entry = new ZipEntry("report.xml");
			/* } else if(file.endsWith(".log")) {
				entry = new ZipEntry("log.txt"); */
			} else {
				String fn = new File(file).getName();

				entry = new ZipEntry("data/"+fn);
			}
			
			System.out.println(entry.getName());
			
			entry.setSize(input.length());
			entry.setTime(input.lastModified());
			entry.setMethod(compression);

			//there has to be a better way to generate the CRC than this!
			crc = new CRC32();
			byte[] readBuffer = new byte[BUFSIZE];
			int bytesRead = 0;
			while(inData.available()>0) {
				bytesRead = inData.read(readBuffer);
				crc.update(readBuffer, 0, bytesRead);
			}
			entry.setCrc(crc.getValue());
			//reset inData, again there has to be a better way
			inData.close();
			inData = new FileInputStream(input);
			
			zip.putNextEntry(entry);			

			readBuffer = new byte[BUFSIZE];
			bytesRead = 0;
			while(inData.available()>0) {
				bytesRead = inData.read(readBuffer);
				zip.write(readBuffer, 0, bytesRead);
			}
			
			inData.close();
			zip.closeEntry();
		}
		
		zip.close();
	}
}
