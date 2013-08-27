/*
 * Copyright (c) 2013 University of Tartu
 */
package org.dmg.pmml;

import java.io.*;

import org.junit.*;

import static org.junit.Assert.*;

public class FieldNameTest {

	@Test
	public void create(){
		assertSame(FieldName.create("x"), FieldName.create("x"));
	}

	@Test
	public void serialization() throws Exception {
		FieldName name = FieldName.create("x");

		assertSame(name, deserializeObject(serializeObject(name)));
	}

	@Test
	public void unmarshal(){
		assertNotNull(FieldName.unmarshal("x"));
	}

	@Test
	public void marshal(){
		assertEquals("x", FieldName.marshal(FieldName.create("x")));
		assertEquals(null, FieldName.marshal(null));
	}

	static
	private byte[] serializeObject(Object object) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);

			try {
				oos.writeObject(object);
			} finally {
				oos.close();
			}
		} finally {
			os.close();
		}

		return os.toByteArray();
	}

	static
	private Object deserializeObject(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);

		try {
			ObjectInputStream ois = new ObjectInputStream(is);

			try {
				return ois.readObject();
			} finally {
				ois.close();
			}
		} finally {
			is.close();
		}
	}
}