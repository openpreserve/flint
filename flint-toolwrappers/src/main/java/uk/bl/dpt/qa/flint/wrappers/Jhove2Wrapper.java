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

//parts based on/from looking at jhove2 app, with the following copyright:
/*
 * JHOVE2 - Next-generation architecture for format-aware characterization
 *
 * Copyright (c) 2010 by The Regents of the University of California,
 * Ithaka Harbors, Inc., and The Board of Trustees of the Leland Stanford
 * Junior University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * o Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * o Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * o Neither the name of the University of California/California Digital
 *   Library, Ithaka Harbors/Portico, or Stanford University, nor the names of
 *   its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

//package uk.bl.dpt.qa.flint.wrappers;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.jhove2.config.spring.SpringConfigInfo;
//import org.jhove2.core.JHOVE2;
//import org.jhove2.core.JHOVE2Exception;
//import org.jhove2.core.io.Input;
//import org.jhove2.core.source.Source;
//import org.jhove2.module.display.Displayer;
//import org.jhove2.module.display.XMLDisplayer;
//import org.jhove2.persist.inmemory.InMemoryFrameworkAccessor;
//import org.jhove2.persist.inmemory.InMemorySourceFactory;
//
///**
// * This class wraps and hides Jhove2 - a call to isValid() will return true or false if
// * Jhove2 thinks the file is valid or not
// * @author wpalmer
// *
// */
//public class Jhove2Wrapper {
//
//	private static JHOVE2 jhove2 = null;
//	
//	private Jhove2Wrapper() {
//		// TODO Auto-generated constructor stub
//	}
//	
//	private static void initJhove2() {
//		try {
//
//			jhove2 = new JHOVE2();
//			
//			jhove2.setSourceFactory(new InMemorySourceFactory());
//			jhove2.setModuleAccessor(new InMemoryFrameworkAccessor());
//			jhove2.setConfigInfo(new SpringConfigInfo());
//			
//
//		} catch (JHOVE2Exception e) {
//			System.err.println("message: "+e.getMessage());
//			e.printStackTrace(System.err);
//		}
//
//	}
//
//	/**
//	 * Queries Jhove2 to see whether a file is valid/well-formed or not
//	 * This method is variously inspired by the Jhove2 sources, tests and javadocs
//	 * @param pFile file to check
//	 * @return true/false if Jhove2 thinks it's valid
//	 */
//	public static boolean isValid(File pFile) {
//		if(null==jhove2) initJhove2();
//
//		boolean ret = false;
//		
//		try {
//
//			List<String> names = new ArrayList<String>();
//			names.add(pFile.getAbsolutePath());
//			
//			// Create a FileSet source unit
//			Source source = jhove2.getSourceFactory().getSource(jhove2, names);
//			source.addModule(jhove2);
//			
//			/* Characterize the FileSet source unit (and all subsidiary
//			 * source units that it encapsulates.
//			 */                     
//			Input input = source.getInput(jhove2);
//			
//			Displayer displayer = new XMLDisplayer(jhove2.getModuleAccessor());
//			displayer.setConfigInfo(jhove2.getConfigInfo());
//			
//			try {
//				source = jhove2.characterize(source, input);
//				//System.out.println(source.getReportableName()+" "+source.getReportableIdentifier());
//
//				displayer.display(source);
//
//				//TODO: do something with the characterize outputs??
//				
//			}
//			finally {
//				if (input != null) {
//					input.close();
//				}
//			}                       
//
//		} catch (IOException e) {
//			e.printStackTrace(System.err);
//		} catch (JHOVE2Exception e) {
//			System.err.println("message: "+e.getMessage());
//			e.printStackTrace(System.err);
//		}
//
//		return ret;
//	}
//	
//
//}
