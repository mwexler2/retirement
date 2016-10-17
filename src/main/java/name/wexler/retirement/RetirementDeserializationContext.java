package name.wexler.retirement;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;

import java.io.IOException;

/**
 * Created by mwexler on 10/16/16.
 */
public class RetirementDeserializationContext extends DefaultDeserializationContext {
    private EntityManager<Job> jobManager;
    private EntityManager<Entity> entityManager;

    public RetirementDeserializationContext(DeserializerFactory df,
                                     DeserializerCache cache,
                                     EntityManager<Job> jobManager,
                                     EntityManager<Entity> entityManager) {
        super(df, cache);
        this.jobManager = jobManager;
        this.entityManager = entityManager;
    }

    public RetirementDeserializationContext(RetirementDeserializationContext src,
                                     DeserializerFactory factory) {
        super(src, factory);
        this.jobManager = src.jobManager;
        this.entityManager = src.entityManager;
    }

    public RetirementDeserializationContext(RetirementDeserializationContext src,
                                     DeserializationConfig config, JsonParser jp, InjectableValues values) {
        super(src, config, jp, values);
        this.jobManager = src.jobManager;
        this.entityManager = src.entityManager;
    }

    @Override
    public DefaultDeserializationContext with(DeserializerFactory factory) {
        return new RetirementDeserializationContext(this, factory);
    }

    @Override
    public DefaultDeserializationContext createInstance(
            DeserializationConfig config, JsonParser jp, InjectableValues values) {
        return new RetirementDeserializationContext(this, config, jp, values);
    }

    @Override
    public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> generator, ObjectIdResolver resolverType) {
        ReadableObjectId oid = super.findObjectId(id, generator, resolverType);

        if (oid.resolve() == null && this.jobManager != null) {
            Job object = this.jobManager.getById((String) id);
            if (object != null)
                try {
                    oid.bindItem(object);
                } catch (IOException ex) {
                    throw new IllegalStateException("Unable to bind " + object + " to " + id, ex);
                }
        }
        return oid;
    }
}
