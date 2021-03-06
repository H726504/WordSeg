package mitlab.seg.ner.perceptron;

import java.io.IOException;
import mitlab.seg.ner.corpus.document.sentence.Sentence;
import mitlab.seg.ner.perceptron.common.TaskType;
import mitlab.seg.ner.perceptron.feature.FeatureMap;
import mitlab.seg.ner.perceptron.instance.Instance;
import mitlab.seg.ner.perceptron.instance.NERInstance;
import mitlab.seg.ner.perceptron.model.LinearModel;
import mitlab.seg.ner.perceptron.tagset.NERTagSet;
import mitlab.seg.ner.tokenizer.lexical.NERecognizer;

/**
 * 命名实体识别
 *
 */
public class PerceptionNERecognizer extends PerceptronTagger implements NERecognizer {
  final NERTagSet tagSet;

  public PerceptionNERecognizer(LinearModel nerModel) {
    super(nerModel);
    if (nerModel.tagSet().type != TaskType.NER) {
      throw new IllegalArgumentException(
          String.format("错误的模型类型: 传入的不是命名实体识别模型，而是 %s 模型", nerModel.featureMap.tagSet.type));
    }
    this.tagSet = (NERTagSet) model.tagSet();
  }

  public PerceptionNERecognizer(String nerModelPath) throws IOException {
    this(new LinearModel(nerModelPath));
  }

  /**
   * 加载配置文件指定的模型
   *
   * @throws IOException
   */
  public PerceptionNERecognizer() throws IOException {
    this(mitlab.seg.Constants.NERMODEL);
  }

  public String[] recognize(String[] wordArray, String[] posArray) {
    NERInstance instance = new NERInstance(wordArray, posArray, model.featureMap);
    instance.tagArray = new int[instance.size()];
    model.viterbiDecode(instance);
    return instance.tags(tagSet);
  }

  @Override
  public NERTagSet getNERTagSet() {
    return tagSet;
  }

  /**
   * 在线学习
   *
   * @param segmentedTaggedNERSentence 人民日报2014格式的句子
   * @return 是否学习成功（失败的原因是参数错误）
   */
  public boolean learn(String segmentedTaggedNERSentence) {
    return learn(NERInstance.create(segmentedTaggedNERSentence, model.featureMap));
  }

  @Override
  protected Instance createInstance(Sentence sentence, FeatureMap featureMap) {
    return NERInstance.create(sentence, featureMap);
  }
}
