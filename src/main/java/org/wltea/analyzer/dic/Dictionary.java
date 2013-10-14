package org.wltea.analyzer.dic;

import java.util.Collection;

import org.apache.zookeeper.KeeperException;

public interface Dictionary {
	
	/**
	 * 检索匹配主词典
	 * @param charArray
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray);

	/**
	 * 检索匹配主词典
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray , int begin, int length);
	
	
	/**
	 * 检索匹配量词词典
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInQuantifierDict(char[] charArray , int begin, int length);
	
	
	/**
	 * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 * @param charArray
	 * @param currentIndex
	 * @param matchedHit
	 * @return Hit
	 */
	public Hit matchWithHit(char[] charArray , int currentIndex , Hit matchedHit);
	
	/**
	 * 判断是否是停止词
	 * @param charArray
	 * @param begin
	 * @param length
	 * @return boolean
	 */
	public boolean isStopWord(char[] charArray , int begin, int length);
	
	
	/**
	 * 批量加载新词条
	 * @param words Collection<String>词条列表
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void addWordsToMainDict(Collection<String> words) throws DictionaryException;
	/**
	 * 批量移除（屏蔽）词条
	 * @param words
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void disableWordsFromMainDict(Collection<String> words) throws DictionaryException;
	
	/**
	 * 批量加载新词条
	 * @param words Collection<String>词条列表
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void addWordsToStopDict(Collection<String> words) throws DictionaryException;
	
	/**
	 * 批量移除（屏蔽）词条
	 * @param words
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void disableWordsFromStopDict(Collection<String> words) throws DictionaryException;
}
