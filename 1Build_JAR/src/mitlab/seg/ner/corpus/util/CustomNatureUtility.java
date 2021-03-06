package mitlab.seg.ner.corpus.util;

import java.util.Map;
import java.util.TreeMap;
import mitlab.seg.ner.corpus.tag.Nature;
import mitlab.seg.ner.dictionary.CoreDictionaryTransformMatrixDictionary;
import mitlab.seg.ner.dictionary.CustomDictionary;
import mitlab.seg.ner.utility.Vertex;

/**
 * 运行时动态增加词性工具
 *
 */
public class CustomNatureUtility {
  static {
    System.out.println("已激活自定义词性功能,由于采用了反射技术,用户需对本地环境的兼容性和稳定性负责!\n"
        + "如果用户代码X.java中有switch(nature)语句,需要调用CustomNatureUtility.registerSwitchClass(X.class)注册X这个类");
  }
  private static Map<String, Nature> extraValueMap = new TreeMap<String, Nature>();


  public static Nature getNature(String name) {

    try {
      return Nature.valueOf(name);
    } catch (Exception e) {
      // 动态添加的词语有可能无法通过valueOf获取
      return extraValueMap.get(name);
    }
  }
}
