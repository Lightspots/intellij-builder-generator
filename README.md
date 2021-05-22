# Intellij Builder Generator

![Build](https://github.com/Lightspots/intellij-builder-generator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->
Plugin which adds a `Builder` action to the generation menu. The action generates a builder as inner class.

````java
public class TestDto {
    private final String field1;
    private final String field2;

    private TestDto(Builder builder) {
        field1 = builder.field1;
        field2 = builder.field2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String field1;
        private String field2;

        private Builder() {
            // use static builder method
        }

        public Builder field1(String val) {
            field1 = val;
            return this;
        }

        public Builder field2(String val) {
            field2 = val;
            return this;
        }

        public TestDto build() {
            return new TestDto(this);
        }
    }
}
````

## Features

* Optional static method for getting the builder
    * Custom name for that static method
* Custom prefix for the builder methods
* Infer nullable / nonNull annotations from field / getters
    * Currently, it is necessary to manually configure the fully qualified class name of the used annotations

## Usage

You can invoke it via <kbd>Alt</kbd>+<kbd>Insert</kbd> (generation dialog) and choose the builder action or directly with <kbd>Shift</kbd>+<kbd>Alt</kbd>+<kbd>B</kbd>.
Then you need to choose the members for which you want to generate builder methods and configure the features.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  intellij-builder-generator"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Lightspots/intellij-builder-generator/releases/latest) and install it
  manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
