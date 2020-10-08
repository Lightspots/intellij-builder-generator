package ch.lightspots.it.intellij.plugin.generate.builder;

public class TestDto {
    private final String field1;
    private final String field2;

    private TestDto(Builder builder) {
        field1 = builder.field1;
        field2 = builder.field2;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private String field1;
        private String field2;

        private Builder() {
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
