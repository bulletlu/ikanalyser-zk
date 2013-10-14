package org.wltea.analyzer.cfg;

import java.util.Properties;

public abstract class AbstractConfiguration implements Configuration {

	/*
	 * 分词器默认字典路径 
	 */
	private static final String PATH_DIC_MAIN = "org/wltea/analyzer/dic/main2012.dic";
	private static final String PATH_DIC_QUANTIFIER = "org/wltea/analyzer/dic/quantifier.dic";

	/*
	 * 分词器配置文件路径
	 */	
	static final String FILE_NAME = "IKAnalyzer.cfg.xml";
	
	//配置属性——扩展字典
	static final String DICT_TYPE = "type";
	//配置属性——扩展字典
	static final String EXT_DICT = "ext_dict";
	//配置属性——扩展停止词典
	static final String EXT_STOP = "ext_stopwords";
	
	Properties props;
	/*
	 * 是否使用smart方式分词
	 */
	boolean useSmart;

	
	@Override
	public DictionaryType getDictionaryType() {
		// TODO Auto-generated method stub
		String type = props.getProperty(DICT_TYPE);
		DictionaryType t = DictionaryType.zk;
		DictionaryType[] types = DictionaryType.values();
		for(DictionaryType tt :types){
			if(tt.getVal().equals(type)){
				t = tt;
				break;
			}
		}
		return t;
	}
	
	/**
	 * 返回useSmart标志位
	 * useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * @return useSmart
	 */
	public boolean useSmart() {
		return useSmart;
	}

	/**
	 * 设置useSmart标志位
	 * useSmart =true ，分词器使用智能切分策略， =false则使用细粒度切分
	 * @param useSmart
	 */
	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}	
	
	/**
	 * 获取主词典路径
	 * 
	 * @return String 主词典路径
	 */
	public String getMainDictionary(){
		return PATH_DIC_MAIN;
	}

	/**
	 * 获取量词词典路径
	 * @return String 量词词典路径
	 */
	public String getQuantifierDicionary(){
		return PATH_DIC_QUANTIFIER;
	}

}
