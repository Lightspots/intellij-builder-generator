# Intellij Builder Generator

![Build](https://github.com/Lightspots/intellij-builder-generator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16845.svg)](https://plugins.jetbrains.com/plugin/16845)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16845.svg)](https://plugins.jetbrains.com/plugin/16845)

<!-- Plugin description -->
Plugin which adds a `Builder` action to the generation menu. The action generates a builder as inner class.

````java
import java.util.Objects;

public class TestDto {
  @Nullable
  private final String field1;
  @NonNull
  private final String field2;

  private TestDto(Builder builder) {
    this.field1 = builder.field1;
    this.field2 = Objects.requireNonNull(builder.field2);
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String field1;
    private String field2;

    private Builder() {
      // use static builder method
    }

    @NonNull
    public Builder field1(@Nullable String field1) {
      this.field1 = field1;
      return this;
    }

    @NonNull
    public Builder field2(@NonNull String field2) {
      this.field2 = field2;
      return this;
    }

    @NonNull
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
* Add requireNonNull for fields (or getters) marked with `@NonNull` annotation

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
