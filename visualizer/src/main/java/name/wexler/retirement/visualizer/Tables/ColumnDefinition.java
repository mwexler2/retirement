package name.wexler.retirement.visualizer.Tables;

public class ColumnDefinition {
    final private String name;
    final private String property;
    final private String href;
    final private String paramProperty;
    final private String decorator;
    final private String paramName;
    final private boolean total;
    final private String className;
    final private String headerClassName;

    public ColumnDefinition(Builder builder)
    {
        this.name = builder.name;
        this.property = builder.property;
        this.href = builder.href;
        this.paramProperty = builder.paramProperty;
        this.decorator = builder.decorator;
        this.total = builder.total;
        this.paramName = builder.paramName;
        this.className = builder.className;
        this.headerClassName = builder.headerClassName;
    }

    // Static class Builder
    public static class Builder {

        /// instance fields
        private String name;
        private String property;
        private String href;
        private String paramProperty;
        private String decorator;
        private String paramName;
        private boolean total;
        private String className;
        private String headerClassName;

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

        public Builder setParamName(String paramName) {
            this.paramName = paramName;
            return this;
        }

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder setHeaderClassName(String headerClassName) {
            this.headerClassName = headerClassName;
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

    public String getClassName() {
        return className;
    }

    public String getHeaderClassName() {
        return headerClassName;
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
