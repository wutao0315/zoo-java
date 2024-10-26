
package com.wxius.framework.zoo.util.function;

import java.util.Date;

import com.wxius.framework.zoo.exceptions.ZooException;
import com.wxius.framework.zoo.util.parser.ParserException;
import com.wxius.framework.zoo.util.parser.Parsers;
import com.google.common.base.Function;


public interface Functions {
  Function<String, Integer> TO_INT_FUNCTION = Integer::parseInt;
  Function<String, Long> TO_LONG_FUNCTION = Long::parseLong;
  Function<String, Short> TO_SHORT_FUNCTION = Short::parseShort;
  Function<String, Float> TO_FLOAT_FUNCTION = Float::parseFloat;
  Function<String, Double> TO_DOUBLE_FUNCTION = Double::parseDouble;
  Function<String, Byte> TO_BYTE_FUNCTION = Byte::parseByte;
  Function<String, Boolean> TO_BOOLEAN_FUNCTION = Boolean::parseBoolean;
  Function<String, Date> TO_DATE_FUNCTION = input -> {
    try {
      return Parsers.forDate().parse(input);
    } catch (ParserException ex) {
      throw new ZooException("Parse date failed", ex);
    }
  };
  Function<String, Long> TO_DURATION_FUNCTION = input -> {
    try {
      return Parsers.forDuration().parseToMillis(input);
    } catch (ParserException ex) {
      throw new ZooException("Parse duration failed", ex);
    }
  };
}
