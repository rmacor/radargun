package org.radargun.stages.stream;

import org.infinispan.stream.CacheCollectors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Roman Macor &lt;rmacor@redhat.com&gt;
 */
public class WordCount implements StreamStage.StreamFunction {

   private Map<Object, Object> result;

   @Override
   public Object apply(Stream stream) {
      Object result = stream.map((Serializable & Function<Map.Entry<String, String>, String[]>) e -> e.getValue().split("[\\p{Punct}\\s&&[^'-]]+")).
              flatMap((Serializable & Function<String[], Stream<String>>) Arrays::stream).
              collect(CacheCollectors.serializableCollector(() -> Collectors.groupingBy(Function.identity(), Collectors.counting())));
      this.result = (Map<Object, Object>) result;
      return result;
   }

   @Override
   public String getPrintableResult() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<Object, Object> entry : result.entrySet()) {
         sb.append(System.getProperty("line.separator"));
         sb.append("key: " + entry.getKey() + " value: " + entry.getValue());
      }
      return sb.toString();
   }

   @Override
   public long getResultCount() {
      return result.keySet().size();
   }
}
