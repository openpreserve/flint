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
package uk.bl.dpt.qa.drmlint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * This class runs an external tool via command line and buffers stdout and stderr
 * This multi-threaded approach has to be used as otherwise Windows hangs 
 * @author wpalmer
 *
 */
public class ToolRunner {

	private boolean gRedirectStderr = false;
	
	/**
	 * Create a new ToolRunner (not redirecting stderr to stdout)
	 */
	public ToolRunner() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Create a new ToolRunner
	 * @param pRedirectStderr whether or not to redirect stderr to stdout
	 */
	public ToolRunner(boolean pRedirectStderr) {
		gRedirectStderr = pRedirectStderr;
	}
	
	private BufferedReader gStdout = null;
	private BufferedReader gStderr = null;
	
	/**
	 * Executes a given command line.  Note stdout and stderr will be populated by this method.
	 * @param pCommandLine command line to run
	 * @return exit code from execution of the command line
	 * @throws IOException error
	 */
	public int runCommand(List<String> pCommandLine) throws IOException {
		//check there are no command line options that are empty
		while(pCommandLine.contains("")) {
			pCommandLine.remove("");
		}
		
		ProcessBuilder pb = new ProcessBuilder(pCommandLine);
		pb.redirectErrorStream(gRedirectStderr);
		//set the working directory to our temporary directory
		//pb.directory(new File(gTempDir));

		//log outputs to file(s) - fixes hangs on windows
		//and logs *all* output (unlike when using IOStreamThread)
		File stdoutFile = File.createTempFile("stdout-log-", ".log");
		stdoutFile.deleteOnExit();
		File stderrFile = File.createTempFile("stderr-log-", ".log");
		stderrFile.deleteOnExit();
		pb.redirectOutput(stdoutFile);
		if(!gRedirectStderr) {
			pb.redirectError(stderrFile);
		}
		
		//start the executable
		Process proc = pb.start();

		try {
			//wait for process to end before continuing
			proc.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//create a log of the console output
		gStdout = new BufferedReader(new FileReader(stdoutFile));
		gStderr = new BufferedReader(new FileReader(stderrFile));
		
		return proc.exitValue();
	}
	
	/**
	 * Get stdout buffer
	 * @return stdout buffer
	 */
	public BufferedReader getStdout() {
		return gStdout;
	}

	/**
	 * Get stderr buffer
	 * @return stderr buffer
	 */
	public BufferedReader getStderr() {
		return gStderr;
	}

}
