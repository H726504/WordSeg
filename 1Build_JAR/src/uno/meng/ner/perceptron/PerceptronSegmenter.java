package uno.meng.ner.perceptron;

import uno.meng.ner.corpus.document.sentence.Sentence;
import uno.meng.ner.perceptron.common.TaskType;
import uno.meng.ner.perceptron.feature.FeatureMap;
import uno.meng.ner.perceptron.instance.CWSInstance;
import uno.meng.ner.perceptron.instance.Instance;
import uno.meng.ner.perceptron.model.LinearModel;
import uno.meng.ner.perceptron.tagset.CWSTagSet;
import uno.meng.ner.perceptron.utility.Utility;
import uno.meng.ner.tokenizer.lexical.Segmenter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 中文分词
 *
 */
public class PerceptronSegmenter extends PerceptronTagger implements Segmenter {
  private final CWSTagSet CWSTagSet;

  public PerceptronSegmenter(LinearModel cwsModel) {
    super(cwsModel);
    if (cwsModel.featureMap.tagSet.type != TaskType.CWS) {
      throw new IllegalArgumentException(
          String.format("错误的模型类型: 传入的不是分词模型，而是 %s 模型", cwsModel.featureMap.tagSet.type));
    }
    CWSTagSet = (CWSTagSet) cwsModel.featureMap.tagSet;
  }

  public PerceptronSegmenter(String cwsModelFile) throws IOException {
    this(new LinearModel(cwsModelFile));
  }

  /**
   * 加载配置文件指定的模型
   * 
   * @throws IOException
   */
  public PerceptronSegmenter() throws IOException {
    this(uno.meng.Constants.CWSMODEL);
  }

  public void segment(String text, List<String> output) {
    String normalized = normalize(text);
    segment(text, normalized, output);
  }

  public void segment(String text, String normalized, List<String> output) {
    if (text.isEmpty())
      return;
    Instance instance = new CWSInstance(normalized, model.featureMap);
    int[] tagArray = instance.tagArray;
    model.viterbiDecode(instance, tagArray);

    StringBuilder result = new StringBuilder();
    result.append(text.charAt(0));

    for (int i = 1; i < tagArray.length; i++) {
      if (tagArray[i] == CWSTagSet.B || tagArray[i] == CWSTagSet.S) {
        output.add(result.toString());
        result.setLength(0);
      }
      result.append(text.charAt(i));
    }
    if (result.length() != 0) {
      output.add(result.toString());
    }
  }

  public List<String> segment(String sentence) {
    List<String> result = new LinkedList<String>();
    segment(sentence, result);
    return result;
  }

  /**
   * 在线学习
   *
   * @param segmentedSentence 分好词的句子，空格或tab分割，不含词性
   * @return 是否学习成功（失败的原因是参数错误）
   */
  public boolean learn(String segmentedSentence) {
    return learn(segmentedSentence.split("\\s+"));
  }

  /**
   * 在线学习
   *
   * @param words 分好词的句子
   * @return 是否学习成功（失败的原因是参数错误）
   */
  public boolean learn(String... words) {
    // for (int i = 0; i < words.length; i++) // 防止传入带词性的词语
    // {
    // int index = words[i].indexOf('/');
    // if (index > 0)
    // {
    // words[i] = words[i].substring(0, index);
    // }
    // }
    return learn(new CWSInstance(words, model.featureMap));
  }

  @Override
  protected Instance createInstance(Sentence sentence, FeatureMap featureMap) {
    return CWSInstance.create(sentence, featureMap);
  }

  @Override
  public double[] evaluate(String corpora) throws IOException {
    // 这里用CWS的F1
    double[] prf = Utility.prf(Utility.evaluateCWS(corpora, this));
    return prf;
  }
}
