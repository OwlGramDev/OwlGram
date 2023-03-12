package it.owlgram.android.magic;

import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.SerializedData;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class MagicBaseObject {
    protected final int VECTOR_CONSTRUCTOR = 0x1cb5c415;
    protected final int HASH_VECTOR_CONSTRUCTOR = 0x1cb5c417;
    protected final int HASH_MAP_VECTOR_CONSTRUCTOR = 0x1cb5c418;
    protected final int STRING_CONSTRUCTOR = 0xb5286e24;
    protected final int INT_CONSTRUCTOR = 0x997275b5;
    protected final int LONG_CONSTRUCTOR = 0x22076cba;
    protected final int DOUBLE_CONSTRUCTOR = 0x2210c154;
    protected final int BOOL_CONSTRUCTOR = 0x997275b6;
    protected final int BYTES_CONSTRUCTOR = 0x1cb5c416;

    private final int MAX_VECTOR_SIZE = 1000;

    public abstract int getConstructor();

    // READING MAGIC OBJECTS
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void readParams(InputStream ios, boolean exception) throws IOException {
        byte[] fileBytes = new byte[ios.available()];
        ios.read(fileBytes);
        readParams(fileBytes, exception);
    }

    public void readParams(byte[] stream, boolean exception) {
        if (stream == null) {
            return;
        }
        readParams(new SerializedData(stream), exception);
    }

    public void readParams(AbstractSerializedData stream, boolean exception) {
        readParams(stream, stream.readInt32(exception), exception);
    }

    public void readParams(AbstractSerializedData stream, int constructor, boolean exception) {
        int currentConstructor = getConstructor();
        if (constructor != currentConstructor) {
            if (exception) {
                throw new RuntimeException(String.format("can't parse magic %x in %s", constructor, getClass().getSimpleName()));
            }
            return;
        }
        HashMap<String, Field> fields = getFields(getClass()).stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getName(), v), HashMap::putAll);
        for (int a = 0; a < fields.size(); a++) {
            String keyFound = stream.readString(exception);
            int constructorFound = stream.readInt32(exception);
            Field field = fields.get(keyFound);
            if (field == null || !fields.containsKey(keyFound)) {
                if (exception) {
                    throw new RuntimeException(String.format("key %s not found in %s", keyFound, getClass().getSimpleName()));
                }
                return;
            }
            try {
                field.setAccessible(true);
                Object result = readObject(stream, constructorFound, exception);
                if (result == null) {
                    return;
                }
                field.set(this, result);
            } catch (IllegalAccessException | SecurityException e) {
                if (exception) {
                    throw new RuntimeException(String.format("can't set value %s for key %s in %s", field.getName(), keyFound, getClass().getSimpleName()));
                }
            }
        }
    }

    private ArrayList<?> readArrayList(AbstractSerializedData stream, boolean exception) {
        ArrayList<Object> result = new ArrayList<>();
        int count = stream.readInt32(exception);
        for (int a = 0; a < Math.min(count, MAX_VECTOR_SIZE); a++) {
            result.add(readObject(stream, stream.readInt32(exception), exception));
        }
        return result;
    }

    private HashSet<?> readHashSet(AbstractSerializedData stream, boolean exception) {
        HashSet<Object> result = new HashSet<>();
        int count = stream.readInt32(exception);
        for (int a = 0; a < Math.min(count, MAX_VECTOR_SIZE); a++) {
            result.add(readObject(stream, stream.readInt32(exception), exception));
        }
        return result;
    }

    private HashMap<?, ?> readHashMap(AbstractSerializedData stream, boolean exception) {
        HashMap<Object, Object> result = new HashMap<>();
        int count = stream.readInt32(exception);
        for (int a = 0; a < Math.min(count, MAX_VECTOR_SIZE); a++) {
            Object key = readObject(stream, stream.readInt32(exception), exception);
            Object value = readObject(stream, stream.readInt32(exception), exception);
            result.put(key, value);
        }
        return result;
    }

    private Object readObject(AbstractSerializedData stream, int constructor, boolean exception) {
        switch (constructor) {
            case VECTOR_CONSTRUCTOR:
                return readArrayList(stream, exception);
            case HASH_VECTOR_CONSTRUCTOR:
                return readHashSet(stream, exception);
            case HASH_MAP_VECTOR_CONSTRUCTOR:
                return readHashMap(stream, exception);
            case STRING_CONSTRUCTOR:
                return stream.readString(exception);
            case INT_CONSTRUCTOR:
                return stream.readInt32(exception);
            case LONG_CONSTRUCTOR:
                return stream.readInt64(exception);
            case DOUBLE_CONSTRUCTOR:
                return stream.readDouble(exception);
            case BOOL_CONSTRUCTOR:
                return stream.readBool(exception);
            case BYTES_CONSTRUCTOR:
                return stream.readByteArray(exception);
        }
        return readCustomObject(stream, constructor, exception);
    }

    //WRITING MAGIC OBJECTS
    public byte[] serializeToStream() {
        SerializedData stream = new SerializedData();
        serializeToStream(stream);
        return stream.toByteArray();
    }

    private ArrayList<Field> getFields(Class<?> context) {
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(context.getDeclaredFields()));
        Class<?> superclass = context.getSuperclass();
        if (superclass != null && !superclass.getSimpleName().equals("MagicBaseObject")) {
            fields.addAll(getFields(superclass));
        }
        return fields;
    }

    private void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(getConstructor());
        for (Field field : getFields(getClass())) {
            String keyFound = field.getName();
            stream.writeString(keyFound);
            serializeObject(field, stream);
        }
    }

    private void serializeArrayList(ArrayList<?> items, AbstractSerializedData stream) {
        int count = items.size();
        stream.writeInt32(count);
        for (int a = 0; a < count; a++) {
            serializeObject(items.get(a), stream);
        }
    }

    private void serializeHashSet(HashSet<?> items, AbstractSerializedData stream) {
        int count = items.size();
        stream.writeInt32(count);
        for (Object item : items) {
            serializeObject(item, stream);
        }
    }

    private void serializeHashMap(HashMap<?, ?> items, AbstractSerializedData stream) {
        int count = items.size();
        stream.writeInt32(count);
        for (Map.Entry<?, ?> entry : items.entrySet()) {
            serializeObject(entry.getKey(), stream);
            serializeObject(entry.getValue(), stream);
        }
    }

    private void serializeObject(Object object, AbstractSerializedData stream) {
        if (object instanceof Field) {
            try {
                Field field = (Field) object;
                field.setAccessible(true);
                object = field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (object instanceof String) {
            stream.writeInt32(STRING_CONSTRUCTOR);
            stream.writeString((String) object);
        } else if (object instanceof Integer) {
            stream.writeInt32(INT_CONSTRUCTOR);
            stream.writeInt32((Integer) object);
        } else if (object instanceof Long) {
            stream.writeInt32(LONG_CONSTRUCTOR);
            stream.writeInt64((Long) object);
        } else if (object instanceof Double) {
            stream.writeInt32(DOUBLE_CONSTRUCTOR);
            stream.writeDouble((Double) object);
        } else if (object instanceof Boolean) {
            stream.writeInt32(BOOL_CONSTRUCTOR);
            stream.writeBool((Boolean) object);
        } else if (object instanceof byte[]) {
            stream.writeInt32(BYTES_CONSTRUCTOR);
            stream.writeByteArray((byte[]) object);
        } else if (object instanceof ArrayList) {
            stream.writeInt32(VECTOR_CONSTRUCTOR);
            serializeArrayList((ArrayList<?>) object, stream);
        } else if (object instanceof HashSet) {
            stream.writeInt32(HASH_VECTOR_CONSTRUCTOR);
            serializeHashSet((HashSet<?>) object, stream);
        } else if (object instanceof HashMap) {
            stream.writeInt32(HASH_MAP_VECTOR_CONSTRUCTOR);
            serializeHashMap((HashMap<?, ?>) object, stream);
        } else if (object instanceof MagicBaseObject && isCustomObject(object)) {
            ((MagicBaseObject) object).serializeToStream(stream);
        } else if (object != null) {
            throw new RuntimeException("can't serialize object " + object);
        }
    }

    private static boolean isCustomObject(Object obj) {
        Class<?>[] classes = OWLENC.class.getClasses();
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }

    private static MagicBaseObject readCustomObject(AbstractSerializedData stream, int constructor, boolean exception) {
        Class<?>[] classes = OWLENC.class.getClasses();
        for (Class<?> clazz : classes) {
            try {
                MagicBaseObject obj = (MagicBaseObject) clazz.newInstance();
                if (obj.getConstructor() == constructor) {
                    obj.readParams(stream, constructor, exception);
                    return obj;
                }
            } catch (IllegalAccessException | InstantiationException ignored) {}
        }
        return null;
    }
}
