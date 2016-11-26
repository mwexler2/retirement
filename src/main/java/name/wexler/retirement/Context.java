package name.wexler.retirement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mwexler on 11/24/16.
 */
public class Context {
    private Map<String, EntityManager> classEntityManager;
    private ObjectMapper mapper;

    public Context() {
        InjectableValues injectableValues = new InjectableValues.Std().addValue("context", this);
        classEntityManager = new HashMap<>();
        mapper = new ObjectMapper();
        mapper.setInjectableValues(injectableValues);
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public <T> T getById(Class clazz, String id) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        T result = (T) entityManager.getById(id);
        return result;
    }

    public <T> void put(Class clazz, String id, T value) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        entityManager.put(id, value);
    }

    private <T> EntityManager<T> getEntityManager(Class clazz) {
        EntityManager<T> result = (EntityManager<T>) classEntityManager.get(clazz.getSimpleName());
        if (result == null) {
            result = new EntityManager<T>();
            classEntityManager.put(clazz.getSimpleName(), result);
        }
        return result;
    }

    public String toJSON(Object o) throws JsonProcessingException {
        ObjectMapper mapper = getObjectMapper();
        ObjectWriter writer = mapper.writer();
        String result = writer.writeValueAsString(o);
        return result;
    }

    public <T> T fromJSON(Class clazz, String json) throws Exception {
        ObjectMapper mapper = getObjectMapper();
        ObjectWriter writer = mapper.writer();
        T result = (T) mapper.readValue(json, clazz);
        return result;
    }

    public <T> T[] fromJSONFileArray(Class clazz, String filePath) throws IOException {
        File entityFile = new File(filePath);
        ObjectMapper mapper = getObjectMapper();
        T[] result = (T[]) mapper.readValue(entityFile, clazz);
        return result;
    }

    public <T> T fromJSONFile(Class clazz, String filePath) throws IOException {
        File entityFile = new File(filePath);
        ObjectMapper mapper = getObjectMapper();
        T result = (T) mapper.readValue(entityFile, clazz);
        return result;
    }
}
