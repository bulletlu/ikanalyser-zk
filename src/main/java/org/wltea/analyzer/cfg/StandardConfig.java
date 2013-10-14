package org.wltea.analyzer.cfg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * 以整个文件夹为词典
 * @author lunianping
 *
 */
public class StandardConfig extends AbstractConfiguration {
	static Logger logger = Logger.getLogger(StandardConfig.class.getName());
	
	private String defaultPath;

	
	public static Configuration getInstance(){
		return new StandardConfig();
	}
	
	private StandardConfig(){		
		props = new Properties();
		//logger.debug("读取配置文件:"+this.getClass().getClassLoader().getResource(FILE_NAME).getPath());
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(FILE_NAME);
		if(input != null){
			try {
				props.loadFromXML(input);
			} catch (InvalidPropertiesFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String path = URLDecoder.decode(this.getClass().getClassLoader().getResource(FILE_NAME).getPath(),"UTF-8");
			File file = new File(path);
			defaultPath = file.getParent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.wltea.analyzer.cfg.Configuration#getExtDictionarys()
	 */
	@Override
	public List<String> getExtDictionarys() {
		List<String> extDictFiles = new ArrayList<String>(2);
		String path = this.getExtDictionaryPath();
		if(path == null){
			return extDictFiles;
		}
		
		File file = new File(path);
		logger.debug(file.getPath());
		if(file.exists() && file.isDirectory()){
			File [] dics = file.listFiles(new DictionaryFilter());
			for(int i=0;i<dics.length;i++){
				extDictFiles.add(dics[i].getPath());
			}
		}
		return extDictFiles;
	}

	/* (non-Javadoc)
	 * @see org.wltea.analyzer.cfg.Configuration#getExtStopWordDictionarys()
	 */
	@Override
	public List<String> getExtStopWordDictionarys() {
		List<String> extDictFiles = new ArrayList<String>(2);
		String path = this.getExtStopDictionaryPath();
		if(path == null){
			return extDictFiles;
		}
		File file = new File(path);
		logger.debug(file.getPath());
		if(file.exists() && file.isDirectory()){
			File [] dics = file.listFiles(new DictionaryFilter());
			for(int i=0;i<dics.length;i++){
				extDictFiles.add(dics[i].getPath());
			}
		}
		return extDictFiles;
	}


	/**
	 * 返回扩展词库路径
	 * @return
	 */
	private String getExtDictionaryPath(){
		String extDictCfg = props.getProperty(EXT_DICT);
		if(extDictCfg == null) return null;
		File absoluteDir = new File(extDictCfg);
		File relativeDir = new File(this.defaultPath+File.separator+extDictCfg);
		if(absoluteDir != null && absoluteDir.exists()){
			return absoluteDir.getPath();
		}
		
		if(relativeDir != null && relativeDir.exists()){
			return relativeDir.getPath();
		}else{
			throw new IllegalStateException(extDictCfg+"不存在");
		}		
	}
	
	/**
	 * 返回停止词库路径
	 * @return
	 */
	private String getExtStopDictionaryPath(){
		String extDictCfg = props.getProperty(EXT_STOP);
		if(extDictCfg == null) return null;
		File absoluteDir = new File(extDictCfg);
		File relativeDir = new File(this.defaultPath+File.separator+extDictCfg);
		if(absoluteDir != null && absoluteDir.exists()){
			return absoluteDir.getPath();
		}
		
		if(relativeDir != null && relativeDir.exists()){
			return relativeDir.getPath();
		}else{
			throw new IllegalStateException(extDictCfg+"不存在");
		}	
	}
	
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		Configuration config = StandardConfig.getInstance();
		List<String> list = config.getExtDictionarys();
		File main = new File(URLDecoder.decode(StandardConfig.class.getClassLoader().getResource(config.getMainDictionary()).getFile(),"UTF-8"));
		System.out.println(main.getPath()+"  "+main.exists());
		//System.out.println("ExtPath: "+config.getExtDictionaryPath());
		for(String ff : list){
			System.out.println(ff);			
		}
	}

}
