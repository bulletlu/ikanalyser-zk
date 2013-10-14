/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * 
 * 
 */
package org.wltea.analyzer.dic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.wltea.analyzer.cfg.Configuration;


/**
 * 词典管理类,单子模式
 */
/**
 * @author kevin
 *
 */
public class DictionaryWithZK implements Dictionary,Watcher{
	
	 private static Logger logger = Logger.getLogger(DictionaryWithZK.class);

	private static final String ZK_DIC_ROOT = "/dictionary";
	private static final String ZK_DIC_EXT = ZK_DIC_ROOT+"/ext";
	private static final String ZK_DIC_STOP = ZK_DIC_ROOT+"/stop";
	
	/*
	 * 词典单子实例
	 */
	private static Dictionary singleton;
	
	/*
	 * 主词典对象
	 */
	private DictSegment _MainDict;
	
	/*
	 * 停止词词典 
	 */
	private DictSegment _StopWordDict;
	/*
	 * 量词词典
	 */
	private DictSegment _QuantifierDict;
	
	/**
	 * 配置对象
	 */
	private Configuration cfg;
	
	/**
	 * 
	 */
	private ZooKeeper	zk;
	
	private DictionaryWithZK(Configuration cfg){
		this.cfg = cfg;
		this.initZk();
		this.loadMainDict();
		//this.loadStopWordDict();
		this.loadQuantifierDict();
	}
	
	/**
	 * 词典初始化
	 * 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
	 * 只有当Dictionary类被实际调用时，才会开始载入词典，
	 * 这将延长首次分词操作的时间
	 * 该方法提供了一个在应用加载阶段就初始化字典的手段
	 * @return Dictionary
	 */
	public static Dictionary initial(Configuration cfg){
		if(singleton == null){
			synchronized(Dictionary.class){
				if(singleton == null){
					singleton = new DictionaryWithZK(cfg);
					return singleton;
				}
			}
		}
		return singleton;
	}
	
	/**
	 * 获取词典单子实例
	 * @return Dictionary 单例对象
	 */
	public static Dictionary getSingleton(){
		if(singleton == null){
			throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
		}
		return singleton;
	}
	
	private void initZk(){
		String zkhost = System.getProperty("zkHost");
		try {
			this.zk = new ZooKeeper(zkhost,5*1000,this);
			
			if(zk.exists(ZK_DIC_ROOT, false) == null){
				zk.create(ZK_DIC_ROOT, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if(zk.exists(ZK_DIC_EXT+".add", true)==null){
				zk.create(ZK_DIC_EXT+".add", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if(zk.exists(ZK_DIC_EXT+".del", true)==null){
				zk.create(ZK_DIC_EXT+".del", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if(zk.exists(ZK_DIC_STOP+".add", true)==null){
				zk.create(ZK_DIC_STOP+".add", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			if(zk.exists(ZK_DIC_STOP+".del", true)==null){
				zk.create(ZK_DIC_STOP+".del", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 批量加载新词条
	 * @param words Collection<String>词条列表
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void addWordsToMainDict(Collection<String> words) throws DictionaryException{
		if(words != null){
			StringBuffer buf = new StringBuffer();
			for(String word : words){
				if (word != null) {
					//批量加载词条到主内存词典中
					buf.append(word).append("\n");
				}
			}
			try {
				this.zk.setData(ZK_DIC_EXT+".add", buf.toString().getBytes(), -1);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DictionaryException("写入zookeeper数据失败");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DictionaryException("写入zookeeper数据失败");
			}
		}
	}
	
	/**
	 * 批量移除（屏蔽）词条
	 * @param words
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void disableWordsFromMainDict(Collection<String> words) throws DictionaryException{
		if(words != null){
			StringBuffer buf = new StringBuffer();
			for(String word : words){
				if (word != null) {
					//批量屏蔽词条
					try {
						this.zk.setData(ZK_DIC_EXT+".del", buf.toString().getBytes(), -1);
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new DictionaryException("写入zookeeper数据失败");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new DictionaryException("写入zookeeper数据失败");
					}
				}
			}
		}
	}
	
	/**
	 * 批量加载新词条
	 * @param words Collection<String>词条列表
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void addWordsToStopDict(Collection<String> words) throws DictionaryException{
		if(words != null){
			StringBuffer buf = new StringBuffer();
			for(String word : words){
				if (word != null) {
					//批量加载词条到主内存词典中
					buf.append(word).append("\n");
				}
			}
			try {
				this.zk.setData(ZK_DIC_STOP+".add", buf.toString().getBytes(), -1);
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DictionaryException("写入zookeeper数据失败");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new DictionaryException("写入zookeeper数据失败");
			}
		}
	}
	
	/**
	 * 批量移除（屏蔽）词条
	 * @param words
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void disableWordsFromStopDict(Collection<String> words) throws DictionaryException{
		if(words != null){
			StringBuffer buf = new StringBuffer();
			for(String word : words){
				if (word != null) {
					//批量屏蔽词条
					try {
						this.zk.setData(ZK_DIC_STOP+".del", buf.toString().getBytes(), -1);
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new DictionaryException("写入zookeeper数据失败");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new DictionaryException("写入zookeeper数据失败");
					}
				}
			}
		}
	}
	
	/**
	 * 检索匹配主词典
	 * @param charArray
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray){
		return _MainDict.match(charArray);
	}
	
	/**
	 * 检索匹配主词典
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray , int begin, int length){
		return _MainDict.match(charArray, begin, length);
	}
	
	/**
	 * 检索匹配量词词典
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInQuantifierDict(char[] charArray , int begin, int length){
		return _QuantifierDict.match(charArray, begin, length);
	}
	
	
	/**
	 * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 * @param charArray
	 * @param currentIndex
	 * @param matchedHit
	 * @return Hit
	 */
	public Hit matchWithHit(char[] charArray , int currentIndex , Hit matchedHit){
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1 , matchedHit);
	}
	
	
	/**
	 * 判断是否是停止词
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return boolean
	 */
	public boolean isStopWord(char[] charArray , int begin, int length){			
		return _StopWordDict.match(charArray, begin, length).isMatch();
	}	
	
	/**
	 * 加载主词典及扩展词典
	 */
	private void loadMainDict(){
		//建立一个主词典实例
		_MainDict = new DictSegment((char)0);
		//读取主词典文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(cfg.getMainDictionary());
        if(is == null){
        	throw new RuntimeException("Main Dictionary not found!!!");
        }
        
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			String theWord = null;
			do {
				theWord = br.readLine();
				if (theWord != null && !"".equals(theWord.trim())) {
					_MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
				}
			} while (theWord != null);
			
		} catch (IOException ioe) {
			System.err.println("Main Dictionary loading exception.");
			ioe.printStackTrace();
			
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	
	/**
	 * 加载量词词典
	 */
	private void loadQuantifierDict(){
		//建立一个量词典实例
		_QuantifierDict = new DictSegment((char)0);
		//读取量词词典文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(cfg.getQuantifierDicionary());
        if(is == null){
        	throw new RuntimeException("Quantifier Dictionary not found!!!");
        }
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is , "UTF-8"), 512);
			String theWord = null;
			do {
				theWord = br.readLine();
				if (theWord != null && !"".equals(theWord.trim())) {
					_QuantifierDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
				}
			} while (theWord != null);
			
		} catch (IOException ioe) {
			System.err.println("Quantifier Dictionary loading exception.");
			ioe.printStackTrace();
			
		}finally{
			try {
				if(is != null){
                    is.close();
                    is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 更新、删除字典中的词汇
	 * @param words
	 * @param update	true:增加词汇，false:删除词汇
	 * @param dict
	 */
	private void updateWordsToDict(Collection<String> words,boolean update,DictSegment dict){
		if(words != null){
			logger.info("Words : "+words.toString());
			for(String word : words){
				if (word != null) {
					//批量加载词条到主内存词典中
					if(update){
						dict.fillSegment(word.trim().toLowerCase().toCharArray());
					}else{
						dict.disableSegment(word.trim().toLowerCase().toCharArray());
					}
				}
			}
		}
	}

	
	/**
	 * 从zookeeper中取得文件内容，并封装成词汇集合
	 * @param path
	 * @return
	 */
	private Collection<String> getDataFromZkFile(String path){
		Collection<String> set = new HashSet<String>();
		byte[] buf = null;
		try {
			buf = this.zk.getData(path, true, null);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(buf != null){
			String[] words = new String(buf).split("\n");
			for(int i=0;i<words.length;i++){
				set.add(words[i]);
			}
		}
		return set;
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	public void process(WatchedEvent event) {
		// TODO Auto-generated method stub
		logger.debug("path: "+event.getPath()+"   EventType: "+event.getType());
		if(event.getType()==Watcher.Event.EventType.NodeDataChanged && event.getPath()!=null){
			String path = event.getPath();
			logger.debug("path: "+path);
			if(path.equals(ZK_DIC_EXT+".add")){
				Collection<String> words = this.getDataFromZkFile(path);
				this.updateWordsToDict(words, true, _MainDict);
			}else if(path.equals(ZK_DIC_EXT+".del")){
				Collection<String> words = this.getDataFromZkFile(path);
				this.updateWordsToDict(words, false, _MainDict);
			}else if(path.equals(ZK_DIC_STOP+".add")){
				Collection<String> words = this.getDataFromZkFile(path);
				this.updateWordsToDict(words, true,_StopWordDict);
			}else if(path.equals(ZK_DIC_STOP+".del")){
				Collection<String> words = this.getDataFromZkFile(path);
				this.updateWordsToDict(words, true, _StopWordDict);
			} 
		}
	}


	
}
