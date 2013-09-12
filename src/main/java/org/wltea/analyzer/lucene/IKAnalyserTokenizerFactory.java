package org.wltea.analyzer.lucene;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;

/**
 * @author lunianping
 *
 */
public class IKAnalyserTokenizerFactory extends TokenizerFactory {

	public void init(Map<String, String> args) {   
        super.init(args);
        this.setUseSmart(args.get("useSmart").toString().equals("true"));
    }

	private boolean useSmart;

	public boolean useSmart() {
		return useSmart;
	}

	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}

	@Override
	public Tokenizer create(Reader input) {
		// TODO Auto-generated method stub
		Tokenizer _IKTokenizer = new IKTokenizer(input, this.useSmart);
		return _IKTokenizer;
	}

}
