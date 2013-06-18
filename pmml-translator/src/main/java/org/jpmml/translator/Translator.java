package org.jpmml.translator;

import org.dmg.pmml.DataField;
import org.jpmml.manager.Consumer;

/**
 * Common interface for model managers that can translate model to java source code
 * 
 * @author asvirsky
 *
 */
public interface Translator extends Consumer {
	
	public String translate(TranslationContext context) throws TranslationException;
	public String translate(TranslationContext context, DataField outputField) throws TranslationException;

}
