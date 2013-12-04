/*
 * Copyright 2013 The British Library/SCAPE Project Consortium
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
package uk.bl.dpt.qa.drmlint.wrappers;

import java.util.regex.Pattern;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.FeatureEnum;

/**
 * This is an implementation of Report that checks (and ignores) certain errors reported by epubcheck
 * @author wpalmer
 *
 */
public class EpubCheckWrapperReport implements Report {

	private boolean encryption = false;
	private boolean isvalid = true;
	private int warnings = 0;
	private int errors = 0;
	private int exceptions = 0;
	
	/**
	 * Return whether or not the validate() call reported encryption of any sort
	 * @return whether or not the validate() call reported encryption of any sort
	 */
	public boolean hasEncryption() {
		return encryption;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isValidAccordingToPolicy() {
		return isvalid;
	}
	
	/**
	 * Constructor for skeleton implementation
	 */
	public EpubCheckWrapperReport() {
	}

	@Override
	public void hint(String resource, int line, int column, String message) {
		
		// we can safely ignore a hint
		
	}

	@Override
	public void info(String resource, FeatureEnum feature, String value) {
		//some info about epubcheck encryption check here: http://code.google.com/p/epubcheck/issues/detail?id=16
		//might want to check content type here and ignore some parts of file as in issue above?
		if(FeatureEnum.HAS_ENCRYPTION==feature) {
			encryption = true;
		}
		
		// we can safely ignore the rest of info

	}

	@Override
	public void warning(String resource, int line, int column, String message) {

		String msg = message.toLowerCase();
		
		if(msg.contains("cannot be decrypted")) {
			isvalid = false;
			return;
		}
		
		if(msg.startsWith("irregular doctype")) {
			// ignore
			return;
		}
		
		if(msg.startsWith("filename contains spaces. consider changing filename such that uri escaping is not necessary")) {
			// ignore
			return;
		}
		
		if(msg.startsWith("deprecated media-type")) {
			// ignore
			return;			
		}
		
		Pattern p = Pattern.compile("^attribute \"(.+)\" not allowed here; expected attribute \"alt\","+
				  					" \"class\", \"dir\", \"height\", \"id\", \"ismap\", \"lang\", \"longdesc\","+
				  					" \"src\", \"style\", \"title\", \"usemap\", \"width\" or \"xml:lang\"$");
		// ignore
		if(p.matcher(msg).find()) { return; }

		p = Pattern.compile("^meta@dtb:uid content '(.*)' should conform to unique-identifier in content.opf: '(.*)'$");
		// ignore
		if(p.matcher(msg).find()) { return; }

		p = Pattern.compile("^item (.*) exists in the zip file, but is not declared in the opf file(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }
		
		p = Pattern.compile("^"+Pattern.quote("text/html is not appropriate for xhtml/ops, use application/xhtml+xml instead")+"(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }
		
		System.out.println("Warning: "+message+"("+resource+")");
		
		//if we get to this point then assume it's a valid warning and return false
		isvalid = false;
		warnings++;
		
	}
	

	@Override
	public void error(String resource, int line, int column, String message) {

		if(message==null) {
			// This may happen when a file contains DRM
			isvalid = false;
			return;
		}
		
		String msg = message.toLowerCase();
		
		if(msg.contains("cannot be decrypted")) {
			isvalid = false;
			return;
		}
		
		//http://www.idpf.org/epub/linking/cfi/
		if(msg.contains("fragment identifier is not defined in")) {
			return;
		}

		// according to http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm this attribute is optional
		if(msg.startsWith("attribute \"file-as\" not allowed here; expected attribute")) {
			return;
		}

		// according to http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm this attribute is optional
		if(msg.startsWith("attribute \"role\" not allowed here; expected attribute")) {
			return;
		}
		
		// according to http://www.idpf.org/epub/20/spec/OPF_2.0.1_draft.htm this attribute is optional
		if(msg.startsWith("attribute \"scheme\" not allowed here; expected attribute")) {
			return;
		}

		if(msg.startsWith("use of deprecated element")) {
			// ignore
			return;
		}

		if(msg.startsWith("mimetype entry missing or not the first in archive")) {
			// ignore
			return;
		}
		
		if(msg.startsWith("file name contains characters disallowed in ocf file names:")) {
			// ignore
			return;
		}
		
		Pattern p = Pattern.compile("^element \"(.*)\" missing required attribute \"(.*)\"(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }

		p = Pattern.compile("^toc attribute references resource with non-ncx mime type; \".*\" is expected(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }

		p = Pattern.compile("^'(.*)': referenced resource is not declared in the opf manifest.(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }
		
		p = Pattern.compile("^non-standard stylesheet resource '(.*)' of type '(.*)'. a fallback must be specified.(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }

		p = Pattern.compile("^value of attribute \".*\" is invalid; must be an xml name without colons(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }
		
		p = Pattern.compile("^element \"(.*)\" not allowed here; expected the element end-tag, text or element"+
							" \"a\", \"abbr\", \"acronym\", \"applet\", \"b\", \"bdo\", \"big\", \"br\", \"cite\","+
							" \"code\", \"del\", \"dfn\", \"em\", \"i\", \"iframe\", \"img\", \"ins\", \"kbd\", \"map\","+
							" \"noscript\", \"ns:svg\", \"object\", \"q\", \"samp\", \"script\", \"small\", \"span\","+
							" \"strong\", \"sub\", \"sup\", \"tt\" or \"var\" ((.*))(.*)$");
		// ignore
		if(p.matcher(msg).find()) { return; }
		
		System.out.println("Error: "+message+"("+resource+")");

		//if we get to this point then assume it's a valid warning and return false
		isvalid = false;
		errors++;

	}

	@Override
	public void exception(String resource, Exception ex) {

		System.out.println("Exception: "+ex.getMessage()+"("+resource+")");
		
		// should assume invalid on any exception 
		isvalid = false;
		exceptions++;

	}

	@Override
	public int getErrorCount() {
		return errors;
	}

	@Override
	public int getExceptionCount() {
		return exceptions;
	}

	@Override
	public int getWarningCount() {
		return warnings;
	}

}
