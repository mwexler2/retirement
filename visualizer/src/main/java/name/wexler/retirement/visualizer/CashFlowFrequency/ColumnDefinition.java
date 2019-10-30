package name.wexler.retirement.visualizer.CashFlowFrequency;

public class ColumnDefinition {
    private String name;
    private String property;
    private String href;
    private String paramProperty;
    private String decorator;
    private boolean total;

    public ColumnDefinition(Builder builder)
    {
        this.name = builder.name;
        this.property = builder.property;
        this.href = builder.href;
        this.paramProperty = builder.paramProperty;
        this.decorator = builder.decorator;
        this.total = builder.total;
    }

    // Static class Builder
    public static class Builder {

        /// instance fields
        private String name;
        private String property;
        private String href;
        private String paramProperty;
        private String decorator;
        private boolean total;

        public static Builder newInstance()
        {
            return new Builder();
        }

        private Builder() {}

        // Setter methods
        public Builder setName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder setProperty(String property)
        {
            this.property = property;
            return this;
        }

        public Builder setHref(String href)
        {
            this.href = href;
            return this;
        }

        public Builder setParamProperty(String paramProperty)
        {
            this.paramProperty = paramProperty;
            return this;
        }

        public Builder setDecorator(String decorator)
        {
            this.decorator = decorator;
            return this;
        }

        public Builder setTotal(boolean total) {
            this.total = total;
            return this;
        }

        public ColumnDefinition build()
        {
            return new ColumnDefinition(this);
        }
    }

    public String getName() {
        return name;
    }

    public String getProperty() {
        return property;
    }

    public String getHref() {
        return href;
    }

    public String getParamProperty() {
        return paramProperty;
    }

    public String getDecorator() { return this.decorator; }

    public boolean getTotal() {
        return this.total;
    }
}
