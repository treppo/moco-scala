package org.treppo.mocoscala.wrapper

import com.github.dreamhead.moco.extractor.PlainExtractor
import com.github.dreamhead.moco.matcher.{ContainMatcher, EndsWithMatcher, EqRequestMatcher, StartsWithMatcher}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ExtractorMatcherTest extends FlatSpec {
  val extractor: PlainExtractor = new PlainExtractor("hello world")
  val matcher: ExtractorMatcher = new ExtractorMatcher(extractor)

  "a extractor matcher" should "be able to do exact match" in {
    (matcher === "hello") shouldBe a[EqRequestMatcher[String]]
  }

  "a extractor matcher" should "be able to do contain match" in {
    (matcher contain "wor") shouldBe a[ContainMatcher[String]]
  }

  "a extractor matcher" should "be able to do startsWith match" in {
    (matcher startsWith "hell") shouldBe a[StartsWithMatcher[String]]
  }

  "a extractor matcher" should "be able to do endsWith match" in {
    (matcher endsWith "hell") shouldBe a[EndsWithMatcher[String]]
  }

}
