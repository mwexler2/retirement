package name.wexler.retirement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mwexler on 11/24/16.
 */
public class Context {

    private class EntityManager<T> {
        private final HashMap<String, T> allEntities = new HashMap<>();

        public EntityManager() {
        }

        public T getById(String id) {
            return allEntities.get(id);
        }

        public void put(String id, T entity) {
            allEntities.put(id, entity);
        }

        void removeAllEntities() {
            allEntities.clear();
        }
    }


    private final Map<String, EntityManager> classEntityManager;
    private final ObjectMapper mapper;

    public Context() {
        InjectableValues injectableValues = new InjectableValues.Std().addValue("context", this);
        classEntityManager = new HashMap<>();
        mapper = new ObjectMapper();
        mapper.setInjectableValues(injectableValues);

        DateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        mapper.setDateFormat(format);
    }

    private ObjectMapper getObjectMapper() {
        return mapper;
    }

    public <T> T getById(Class clazz, String id) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        T result = entityManager.getById(id);
        return result;
    }

    public <T> void put(Class clazz, String id, T value) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        entityManager.put(id, value);
    }

    private <T> EntityManager<T> getEntityManager(Class clazz) {
        EntityManager<T> result = (EntityManager<T>) classEntityManager.get(clazz.getSimpleName());
        if (result == null) {
            result = new EntityManager<>();
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
