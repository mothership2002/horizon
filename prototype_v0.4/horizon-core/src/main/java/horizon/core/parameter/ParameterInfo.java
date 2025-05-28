package horizon.core.parameter;

import java.lang.reflect.Parameter;

/**
 * Information about a method parameter.
 */
public class ParameterInfo {
    private Parameter parameter;
    private Class<?> type;
    private int index;
    private ParameterSource source;
    private String name;
    private boolean required = true;
    private String defaultValue;

    // Private constructor for builder
    private ParameterInfo() {}

    // Getters
    public Parameter getParameter() {
        return parameter;
    }

    public Class<?> getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public ParameterSource getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ParameterInfo info;

        private Builder() {
            info = new ParameterInfo();
        }

        public Builder parameter(Parameter parameter) {
            info.parameter = parameter;
            return this;
        }

        public Builder type(Class<?> type) {
            info.type = type;
            return this;
        }

        public Builder index(int index) {
            info.index = index;
            return this;
        }

        public Builder source(ParameterSource source) {
            info.source = source;
            return this;
        }

        public Builder name(String name) {
            info.name = name;
            return this;
        }

        public Builder required(boolean required) {
            info.required = required;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            info.defaultValue = defaultValue;
            return this;
        }

        public ParameterInfo build() {
            return info;
        }
    }
}