package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.translator.ContentsItemBuilder;
import fitnesse.wikitext.translator.VariableSource;
import org.junit.Test;
import util.Maybe;

import static org.junit.Assert.assertEquals;

public class ContentsItemTest {
    @Test
    public void buildsPlain() throws Exception {
        assertBuilds("PlainItem", new String[] {}, "", "", "<a href=\"PlainItem\">PlainItem</a>");
    }

    @Test
    public void buildsWithHelp() throws Exception {
        assertBuilds("PlainItem", new String[]{"Help=help"}, "", "", "<a href=\"PlainItem\" title=\"help\">PlainItem</a>");
    }

    @Test
    public void buildsFilter() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Suites=F1"}, "-f", "FILTER_TOC", "<a href=\"PlainItem\">PlainItem (F1)</a>");
    }

    @Test
    public void buildsHelp() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Help=help"}, "-h", "HELP_TOC", "<a href=\"PlainItem\">PlainItem</a><span class=\"pageHelp\">: help</span>");
    }

    @Test
    public void buildsProperties() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Suite=true", "Test=true", "WikiImport=true", "Prune=true"}, "-p", "PROPERTY_TOC",
                "<a href=\"PlainItem\">PlainItem *+@-</a>");
    }

    @Test
    public void buildsRegraced() throws Exception {
        assertBuildsOption("PlainItem", new String[]{}, "-g", "REGRACE_TOC", "<a href=\"PlainItem\">Plain Item</a>");
    }

    private void assertBuildsOption(String page, String[] properties, String option, String variable, String result) throws Exception {
        assertBuilds(page, properties, option, "", result);
        assertBuilds(page, properties, "", variable, result);
    }

    private void assertBuilds(String page, String[] properties, String option, String variable, String result) throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        contents.add(new Symbol(SymbolType.Text, option));
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource(variable, "true"), 1);
        assertEquals(result + HtmlElement.endl, builder.buildItem(withProperties(new TestRoot().makePage(page), properties)).html());
    }

    private WikiPage withProperties(WikiPage page, String[] propList) throws Exception {
        PageData data = page.getData();
        WikiPageProperties props = data.getProperties();
        for (int i = 0; i < propList.length; i++) {
            String[] parts = propList[i].split("=");
            if (parts.length == 1) props.set(parts[0]);
            else props.set(parts[0], parts[1]);
          }

        page.commit(data);
        return page;
    }

}
