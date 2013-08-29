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

import java.io.File;
import java.io.IOException;

import uk.bl.dpt.qa.drmlint.Tools;
import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.JhoveException;
import edu.harvard.hul.ois.jhove.handler.XmlHandler;

/**
 * This class wraps and hides Jhove - a call to isValid() will return true or false if
 * Jhove thinks the file is valid or not
 * @author wpalmer
 *
 */
public class Jhove1Wrapper {

	private static JhoveBase jhove = null;
	private static App app = null;
	private static XmlHandler handler = null;
	
	private Jhove1Wrapper() {
		// TODO Auto-generated constructor stub
	}
	
	private static void initJhove() {
		//http://www.garymcgath.com/jhovenote.html
		//and https://github.com/openplanets/planets-suite/blob/59d1517b5493815a0f59927d6c97ca5462d1ed8d/services/jhove/src/main/java/eu/planets_project/ifr/core/services/identification/jhove/impl/JhoveIdentification.java
		try {
			jhove = new JhoveBase();
		} catch (JhoveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		app = new App(JhoveBase._name, JhoveBase._release, JhoveBase.DATE, "", "");
		handler = new XmlHandler();
		try {
			jhove.init("jhove.conf", JhoveBase.getSaxClassFromProperties());
		} catch (JhoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jhove.setEncoding("utf-8");
		jhove.setTempDirectory(System.getProperty("java.io.tmpdir"));
		jhove.setBufferSize(4096);
		jhove.setChecksumFlag(false);
		jhove.setShowRawFlag(false);
		jhove.setSignatureFlag(false);
	}

	/**
	 * Queries Jhove to see whether a file is valid/well-formed or not
	 * @param pFile file to check
	 * @return true/false if Jhove thinks it's valid
	 */
	public static boolean isValid(File pFile) {
		if(null==jhove) initJhove();

		boolean ret = false;
		try {
			File temp = File.createTempFile("jhove-output-", ".xml");
			String[] inputs = new String[] { pFile.getAbsolutePath() };
			jhove.dispatch(app, null, null, handler, temp.getAbsolutePath(), inputs);
			
			//THIS IS A HORRID HORRID HACK TO GET AROUND JHOVE NAMESPACE ISSUES WHEN NOT ABLE TO CONNECT TO INTERNET
			String xpath = "/*[local-name()='jhove']/*[local-name()='repInfo']/*[local-name()='status']/text()";
			
			String status = Tools.getXpathVal(temp, xpath);
			if(null==status) {
				System.out.println(temp.getAbsolutePath()+": NULL");
			} else {
				if(status.toLowerCase().equals("Well-Formed and valid".toLowerCase())) {
					ret = true;
				}
				//System.out.println(temp.getAbsolutePath()+": "+status+", "+status.length());
			}
			temp.delete();

		} catch (JhoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

}
