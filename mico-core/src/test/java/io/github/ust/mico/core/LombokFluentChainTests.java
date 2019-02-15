package io.github.ust.mico.core;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
class Person {

    private String name = "John Doe";

    private int age = 25;

    private List<String> elements = new ArrayList<>();

    Person(final String name) {
        this.name = name;
    }
}

public class LombokFluentChainTests {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void testLombokFluentChainAccessors() {

        ArrayList<String> elements = new ArrayList<>();
        elements.add("content");

        collector.checkThat("NoArgsConstructor failed",
            new Person().toString(),
            equalTo("Person(name=John Doe, age=25, elements=[])"));

        collector.checkThat("Constructor with single argument failed",
            new Person("John Watson").toString(),
            equalTo("Person(name=John Watson, age=25, elements=[])"));

        collector.checkThat("AllArgsConstructor failed",
            new Person("Sherlock Holmes", 30, elements).toString(),
            equalTo("Person(name=Sherlock Holmes, age=30, elements=[content])"));

        collector.checkThat("Chain with setting one property failed",
            new Person().setName("Jane Doe").toString(),
            equalTo("Person(name=Jane Doe, age=25, elements=[])"));

        collector.checkThat("Chain with setting two properties failed",
            new Person().setName("Jane Doe").setAge(18).toString(),
            equalTo("Person(name=Jane Doe, age=18, elements=[])"));

        collector.checkThat("Chain with setting array property failed",
            new Person().setName("Jane Doe").setAge(18).setElements(elements).toString(),
            equalTo("Person(name=Jane Doe, age=18, elements=[content])"));

        Person testWithArray = new Person().setName("Jane Doe").setAge(18).setElements(elements);
        testWithArray.getElements().add("content2");
        collector.checkThat("Chain with adding element to existing array failed", testWithArray.toString(),
            equalTo("Person(name=Jane Doe, age=18, elements=[content, content2])"));
    }
}
