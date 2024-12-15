package org.demacia;

import org.demacia.rule.RuleMapping;
import org.demacia.value.MapValueStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MapValueStrategyTest {

    @InjectMocks
    private MapValueStrategy mapValueStrategy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getValue_NestedKeyPresent_ReturnsCorrectValue() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("a", new HashMap<String, Object>() {{
            put("b", new HashMap<String, Object>() {{
                put("c", "value");
            }});
        }});

        RuleMapping rule = new RuleMapping().setSource("a.b.c").setDefaultValue("default");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertEquals("value", value);
    }

    @Test
    public void getValue_NestedKeyAbsent_ReturnsDefaultValue() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("a", new HashMap<String, Object>() {{
            put("b", new HashMap<String, Object>() {{
                // "c" key is intentionally missing
            }});
        }});

        RuleMapping rule = new RuleMapping().setSource("a.b.c").setDefaultValue("default");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertEquals("default", value);
    }

    @Test
    public void getValue_FirstLevelKeyAbsent_ReturnsDefaultValue() {
        Map<String, Object> sourceMap = new HashMap<>();

        RuleMapping rule = new RuleMapping().setSource("a").setDefaultValue("default");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertEquals("default", value);
    }

    @Test
    public void getValue_FirstLevelKeyPresentButNestedStructureAbsent_ReturnsDefaultValue() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("a", "someValue");

        RuleMapping rule = new RuleMapping().setSource("a.b.c").setDefaultValue("default");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertEquals("default", value);
    }

    @Test
    public void getValue_NoNestedStructurePresent_ReturnsValueIfDirectMatch() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("a", "value");

        RuleMapping rule = new RuleMapping().setSource("a").setDefaultValue("default");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertEquals("value", value);
    }

    @Test
    public void getValue_NoMatchAndNoDefaultValue_ReturnsNull() {
        Map<String, Object> sourceMap = new HashMap<>();

        RuleMapping rule = new RuleMapping().setSource("a.b.c");

        Object value = mapValueStrategy.getValue(sourceMap, rule);
        assertNull(value);
    }
}
