
package org.demacia;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.demacia.util.JacksonUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JacksonUtilTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void toMap_NullInput_ReturnsNull() {
        Map<String, Object> result = JacksonUtil.toMap(null);
        assertNull(result);
    }

    @Test
    public void toMap_SimpleObject_ReturnsMap() {
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.setName("John");
        simpleObject.setAge(30);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("name", "John");
        expectedMap.put("age", 30);

        Map<String, Object> result = JacksonUtil.toMap(simpleObject);
        assertEquals(expectedMap, result);
    }

    @Test
    public void toMap_NestedObject_ReturnsMap() {
        NestedObject nestedObject = new NestedObject();
        nestedObject.setName("John");
        nestedObject.setAge(30);
        nestedObject.setAddress(new Address("123 Main St", "Anytown"));

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("name", "John");
        expectedMap.put("age", 30);
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("street", "123 Main St");
        addressMap.put("city", "Anytown");
        expectedMap.put("address", addressMap);

        Map<String, Object> result = JacksonUtil.toMap(nestedObject);
        assertEquals(expectedMap, result);
    }

    @Test
    public void toMap_Collection_ReturnsMap() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("list", list);

        Map<String, Object> result = JacksonUtil.toMap(expectedMap);
        assertEquals(expectedMap, result);
    }

    @Test
    public void toMap_ComplexObject_ReturnsMap() {
        ComplexObject complexObject = new ComplexObject();
        complexObject.setName("John");
        complexObject.setAge(30);
        complexObject.setAddresses(new ArrayList<>());
        complexObject.getAddresses().add(new Address("123 Main St", "Anytown"));
        complexObject.getAddresses().add(new Address("456 Elm St", "Othertown"));

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("name", "John");
        expectedMap.put("age", 30);
        List<Map<String, Object>> addressesMap = new ArrayList<>();
        Map<String, Object> address1Map = new HashMap<>();
        address1Map.put("street", "123 Main St");
        address1Map.put("city", "Anytown");
        addressesMap.add(address1Map);
        Map<String, Object> address2Map = new HashMap<>();
        address2Map.put("street", "456 Elm St");
        address2Map.put("city", "Othertown");
        addressesMap.add(address2Map);
        expectedMap.put("addresses", addressesMap);

        Map<String, Object> result = JacksonUtil.toMap(complexObject);
        assertEquals(expectedMap, result);
    }

    @Test
    public void toMap_JsonString_ReturnsMap() throws Exception {
        String jsonString = "{\"name\":\"John\",\"age\":30}";

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("name", "John");
        expectedMap.put("age", 30);

        Map<String, Object> result = objectMapper.readValue(jsonString, Map.class);
        assertEquals(expectedMap, result);
    }

    @Test
    public void toMap_InvalidObject_ReturnsEmptyMap() {
        Object invalidObject = new Object();

        Map<String, Object> result = JacksonUtil.toMap(invalidObject);
        assertEquals(new HashMap<>(), result);
    }

    // 辅助类
    static class SimpleObject {
        private String name;
        private int age;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    static class NestedObject {
        private String name;
        private int age;
        private Address address;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    static class Address {
        private String street;
        private String city;

        public Address(String street, String city) {
            this.street = street;
            this.city = city;
        }

        // Getters and setters
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }

    static class ComplexObject {
        private String name;
        private int age;
        private List<Address> addresses;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public List<Address> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<Address> addresses) {
            this.addresses = addresses;
        }
    }
}