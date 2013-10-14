package org.wltea.analyzer.dic;

import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.Configuration.DictionaryType;

/**
 * @author lunianping
 *
 */
public class DictionaryGenerator {
	
	private static DictionaryType type = DictionaryType.zk;
	
	/**
	 * @param cfg
	 */
	public static void initial(Configuration cfg){
		DictionaryGenerator.type = cfg.getDictionaryType();
		switch(DictionaryGenerator.type){
		case zk :
			DictionaryWithZK.initial(cfg);
			break;
		case local :
			DefaultDictionary.initial(cfg);
			break;
		}
	}
	
	public static Dictionary generate(){
		Dictionary singleton = null;
		switch(DictionaryGenerator.type){
		case zk :
			singleton = DictionaryWithZK.getSingleton();
			break;
		case local :
			singleton = DefaultDictionary.getSingleton();
			break;
		}
		return singleton;
	}

}
