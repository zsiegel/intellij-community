package com.jetbrains.python.fixtures;

import com.jetbrains.python.inspections.PyInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Helps you to create inspection tests.
 * <br/>
 * For each case
 * <ol>
 * <li>Create file <code>testData/inspections/_YOUR_INSPECTION_CLASS_SIMPLE_NAME_/CASE_NAME_CAMEL_CASE.py</code></li>
 * <li>Create method <code>test_YOUR_CASE_NAME_PASCAL_CASE</code> that runs {@link #doTest()}</li>
 * <li>Overwrite {@link #isInfo()}, {@link #isWarning()} or {@link #isWeakWarning()} to configure what to check</li>
 * </ol>
 *
 * @author link
 */
public abstract class PyInspectionTestCase extends PyTestCase {

  @NotNull
  protected abstract Class<? extends PyInspection> getInspectionClass();

  /**
   * Launches test. To be called by test author
   */
  protected void doTest() {
    myFixture.configureByFile("inspections/" + getInspectionClass().getSimpleName() + "/" + getTestName(true) + ".py");
    myFixture.enableInspections(getInspectionClass());
    myFixture.checkHighlighting(isWarning(), isInfo(), isWeakWarning());
  }


  protected boolean isWeakWarning() {
    return true;
  }

  protected boolean isInfo() {
    return false;
  }

  protected boolean isWarning() {
    return true;
  }
}
