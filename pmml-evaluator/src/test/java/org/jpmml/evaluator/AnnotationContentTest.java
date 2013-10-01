/*
 * Copyright, KNIME.com AG, Zurich, Switzerland
 */
package org.jpmml.evaluator;

import java.util.*;

import org.dmg.pmml.*;

import org.junit.*;

import static org.junit.Assert.*;

public class AnnotationContentTest extends PMMLTest {

	@Test
	public void mixedContent() throws Exception {
		PMML pmml = loadPMML(getClass());

		Header header = pmml.getHeader();

		List<Annotation> annotations = header.getAnnotations();

		Annotation annotation = annotations.get(0);

		List<Object> content = annotation.getContent();

		assertEquals(5, content.size());

		assertEquals("First text value", content.get(0));
		assertEquals(Arrays.asList("First extension"), ((Extension)content.get(1)).getContent());
		assertEquals("Second text value", content.get(2));
		assertEquals(Arrays.asList("Second extension"), ((Extension)content.get(3)).getContent());
		assertEquals("Third text value", content.get(4));
	}
}