package org.jpmml.translator;

import org.jpmml.manager.Consumer;

/**
 * Common interface for model managers that can translate model to java source code
 * 
 * @author asvirsky
 *
 */
public interface Translator extends Consumer {
	
	public String translate(TranslationContext context) throws TranslationException;

}
