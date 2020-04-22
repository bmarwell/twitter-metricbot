package io.github.bmhm.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.bmhm.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmhm.twitter.metricbot.conversion.FractionUtil;
import io.github.bmhm.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmhm.twitter.metricbot.conversion.UnitConversion;
import io.micronaut.context.annotation.Prototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class TeapoonConverter implements UsUnitConverter {

  private static final long serialVersionUID = -3692749135515082850L;

  private static final Logger LOG = LoggerFactory.getLogger(TeapoonConverter.class);

  private static final Pattern PATTERN_TSP = Pattern.compile(
      "\\b((?:[0-9]+,)?(?:[0-9]+\\.)?[0-9\\u00BC-\\u00BE\\u2150-\\u215E\\/]+)\\s?(?:tsp|teaspoon)(?:s)?\\b",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

  private static final double GRAMS_PER_TSP = 4.2;

  private static final NumberFormat TBSP_NUMBER_FORMAT = DecimalFormats.atMostTwoFractionDigits();
  private static final NumberFormat GRAM_NUMBER_FORMAT = DecimalFormats.noFractionDigits();

  @Override
  public List<String> getSearchTerms() {
    return singletonList("tsp");
  }

  @Override
  public boolean matches(final String text) {
    if (null == text || text.isEmpty()) {
      return false;
    }

    return PATTERN_TSP.matcher(text).find();
  }

  @Override
  public Collection<UnitConversion> getConvertedUnits(final String text) {
    if (text == null || text.isEmpty()) {
      return emptyList();
    }

    final List<UnitConversion> conversions = new ArrayList<>();

    final Matcher matcher = PATTERN_TSP.matcher(text);
    while (matcher.find()) {
      try {
        final String tbspText = matcher.group(1).replaceAll(",", "");
        final String tbspTextDecimal = FractionUtil.replaceFractions(tbspText);
        final double tbspDecimal = Double.parseDouble(tbspTextDecimal);

        final double grams = tbspDecimal * GRAMS_PER_TSP;

        final ImmutableUnitConversion conversion = ImmutableUnitConversion.builder()
            .inputAmount(TBSP_NUMBER_FORMAT.format(tbspDecimal))
            .inputUnit("tsp")
            .metricAmount(GRAM_NUMBER_FORMAT.format(grams))
            .metricUnit("g")
            .build();

        conversions.add(conversion);
      } catch (final NumberFormatException | ArithmeticException nfe) {
        LOG.error("Unable to parse: [{}].", text, nfe);
      }

    }

    return unmodifiableList(conversions);
  }
}
