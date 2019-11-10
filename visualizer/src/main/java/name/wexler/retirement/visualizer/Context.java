package name.wexler.retirement.visualizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import name.wexler.retirement.visualizer.Entity.Entity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mwexler on 11/24/16.
 */
public class Context {
    private static final Path userHome = Paths.get(System.getProperty("user.home"));
    private static final Path retirementDir = userHome.resolve(".retirement");
    private static final Path resourceDir = retirementDir.resolve("resources");
    private static final Path historyDir = retirementDir.resolve("history");
    private static final Path txnDir = historyDir.resolve("txns");

    private class EntityManager<T extends Entity> {
        private final HashMap<String, T> allEntities = new HashMap<>();

        public EntityManager() {
        }

        public T getById(String id) {
            return allEntities.get(id);
        }

        public T getByName(String name) {
            Entity result = null;
            for (String id : allEntities.keySet()) {
                Entity entity = allEntities.get(id);
                if (entity.getName().equals(name))
                    result = entity;
            }
            return (T) result;
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
    private Assumptions assumptions;

    public Context() {
        InjectableValues injectableValues = new InjectableValues.Std().addValue("context", this);
        classEntityManager = new HashMap<>();
        mapper = new ObjectMapper();
        mapper.setInjectableValues(injectableValues);

        DateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        mapper.setDateFormat(format);
    }

    public void setAssumptions(Assumptions assumptions) {
        this.assumptions = assumptions;
    }

    public Assumptions getAssumptions() {
        return assumptions;
    }

    private ObjectMapper getObjectMapper() {
        return mapper;
    }

    public <T extends Entity> T getById(Class clazz, String id) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        T result = entityManager.getById(id);
        return result;
    }

    public <T extends Entity> T getByName(Class clazz, String name) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        T result = entityManager.getByName(name);
        return result;
    }

    public <T extends Entity> List<T> getListById(Class clazz, String id) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        List<T> result = Collections.singletonList(entityManager.getById(id));
        return result;
    }

    public <T extends Entity> List<T> getByIds(Class clazz, List<String> ids) {
        List<T> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            EntityManager<T> entityManager = this.getEntityManager(clazz);
            T entity = entityManager.getById(id);
            result.add(entity);
        }
        return result;
    }

    public <T extends Entity> void put(Class clazz, String id, T value) {
        EntityManager<T> entityManager = this.getEntityManager(clazz);
        entityManager.put(id, value);
    }

    private <T extends Entity> EntityManager<T> getEntityManager(Class clazz) {
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

    public <T extends Entity> T fromJSON(Class clazz, String json) throws Exception {
        ObjectMapper mapper = getObjectMapper();

        T result = (T) mapper.readValue(json, clazz);
        return result;
    }

    private <T> T[] fromJSONFileArray(Class clazz, String fileName) throws IOException {
        Path filePath = resourceDir.resolve(fileName);
        File entityFile = filePath.toFile();
        ObjectMapper mapper = getObjectMapper();
        try {
            T[] result = (T[]) mapper.readValue(entityFile, clazz);
            return result;
        } catch (MismatchedInputException mie) {
            throw new IOException("Can't parse " + filePath, mie);
        }
    }

    public <T> List<T> fromJSONFileList(Class clazz, String fileName) throws IOException {
        return Arrays.asList(fromJSONFileArray(clazz, fileName));
    }

    public <T> T fromJSONFile(Class clazz, String fileName) throws IOException {
        Path filePath = resourceDir.resolve(fileName);
        File entityFile = filePath.toFile();
        ObjectMapper mapper = getObjectMapper();
        T result = (T) mapper.readValue(entityFile, clazz);
        return result;
    }

    public Path getHistoryDir(String companyId) {
        Path dirPath = txnDir.resolve(companyId);
        return dirPath;
    }
}
