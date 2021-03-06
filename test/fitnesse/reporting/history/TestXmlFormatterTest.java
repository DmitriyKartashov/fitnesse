package fitnesse.reporting.history;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.text.ParseException;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.testrunner.WikiTestPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;
import fitnesse.FitNesseContext;
import fitnesse.reporting.history.TestExecutionReport.TestResult;
import fitnesse.reporting.history.TestXmlFormatter.WriterFactory;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class TestXmlFormatterTest {
  private static final String TEST_TIME = "4/13/2009 15:21:43";
  private DateAlteringClock clock;
  
  @Before
  public void setUp() throws ParseException {
    clock = new DateAlteringClock(DateTimeUtil.getDateFromString(TEST_TIME)).freeze();
  }

  @After
  public void tearDown() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void makeFileName() throws Exception {
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    assertEquals(
      "20090413152143_1_2_3_4.xml", 
      SuiteResponder.makeResultFileName(summary, clock.currentClockTimeInMillis()));
  }
  
  @Test
  public void processTestResultsShouldBuildUpCurrentResultAndFinalSummary() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("name", "content", null));
    page.getData().setAttribute(PageData.PropertySUITES, "tag1");
    WriterFactory writerFactory = mock(WriterFactory.class);
    final TestResult testResult = new TestResult();
    TestXmlFormatter formatter = new TestXmlFormatter(context , page.getSourcePage(), writerFactory) {
      @Override
      protected TestResult newTestResult() {
        return testResult;
      }
    };
    final long startTime = clock.currentClockTimeInMillis();

    formatter.testOutputChunk("outputChunk");

    formatter.testStarted(page);

    clock.elapse(27);

    TestSummary summary = new TestSummary(9,8,7,6);
    formatter.testComplete(page, summary);
    assertThat(formatter.testResponse.getFinalCounts(), equalTo(new TestSummary(0,1,0,0)));
    assertThat(formatter.testResponse.getResults().size(), is(1));
    assertThat(formatter.testResponse.getResults().get(0), is(testResult));
    assertThat(testResult.startTime, is(startTime));
    assertThat(testResult.content, is("outputChunk"));
    assertThat(testResult.right, is("9"));
    assertThat(testResult.wrong, is("8"));
    assertThat(testResult.ignores, is("7"));
    assertThat(testResult.exceptions, is("6"));
    assertThat(testResult.runTimeInMillis, is("27"));
    assertThat(testResult.relativePageName, is(page.getName()));
    assertThat(testResult.tags, is("tag1"));
  }
  
  @Test
  public void allTestingCompleteShouldSetTotalRunTime() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiPage page = new WikiPageDummy("name", "content", null);
    WriterFactory writerFactory = mock(WriterFactory.class);
    TestXmlFormatter formatter = new TestXmlFormatter(context , page, writerFactory) {
      @Override
      protected void writeResults() {
      }
    };

    clock.elapse(77L);
    formatter.close();
    assertThat(formatter.testResponse.getTotalRunTimeInMillis(), is(77L));
  }
}
