/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.io.*;

import org.jpmml.manager.*;

public class CopyExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 2){
			System.err.println("Usage: java " + CopyExample.class.getName() + " <Source file> <Destination file>");

			System.exit(-1);
		}

		File srcFile = new File(args[0]);
		File destFile = new File(args[1]);

		copyPmml(srcFile, destFile);
	}

	static
	public void copyPmml(File srcFile, File destFile) throws Exception {
		IOUtil.marshal(IOUtil.unmarshal(srcFile), destFile);
	}

}