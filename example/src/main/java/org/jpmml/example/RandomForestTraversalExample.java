/*
 * Copyright (c) 2012 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RandomForestTraversalExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 1){
			System.out.println("Usage: java " + RandomForestTraversalExample.class.getName() + " <PMML file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		traverse(pmml);
	}

	static
	private void traverse(PMML pmml){
		RandomForestManager randomForestModelManager = new RandomForestManager(pmml);

		List<Segment> segments = randomForestModelManager.getSegments();
		for(Segment segment : segments){
			TreeModelManager treeModelManager = new TreeModelManager(pmml, (TreeModel)segment.getModel());

			System.out.println("String segment_" + segment.getId() + "(){");

			TreeModelTraversalExample.format(treeModelManager.getOrCreateRoot(), "\t");

			System.out.println("}");
		}
	}
}