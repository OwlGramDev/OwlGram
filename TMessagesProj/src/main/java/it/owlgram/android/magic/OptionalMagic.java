package it.owlgram.android.magic;


import org.telegram.tgnet.AbstractSerializedData;

import java.util.NoSuchElementException;
import java.util.Objects;

public class OptionalMagic<T> extends MagicBaseObject {
    private static final OptionalMagic<?> EMPTY = new OptionalMagic<>();
    private T value;

    private OptionalMagic() {
        this.value = null;
    }

    public static<T> OptionalMagic<T> empty() {
        @SuppressWarnings("unchecked")
        OptionalMagic<T> t = (OptionalMagic<T>) EMPTY;
        return t;
    }

    private OptionalMagic(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> OptionalMagic<T> readParams(byte[] stream, T object) {
        if (object instanceof MagicBaseObject) {
            try {
                ((MagicBaseObject) object).readParams(stream, true);
                return new OptionalMagic<>(object);
            } catch (RuntimeException ignored) {}
        }
        return empty();
    }

    @Override
    public void readParams(AbstractSerializedData stream, int constructor, boolean exception) {
        if (stream.readBool(exception)) {
            if (value instanceof MagicBaseObject) {
                ((MagicBaseObject) value).readParams(stream, exception);
            }
        }
    }

    @Override
    protected void serializeToStream(AbstractSerializedData stream) {
        stream.writeInt32(getConstructor());
        stream.writeBool(isPresent());
        if (isPresent()) {
            if (value instanceof MagicBaseObject) {
                ((MagicBaseObject) value).serializeToStream(stream);
            }
        }
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalMagic)) {
            return false;
        }

        OptionalMagic<?> other = (OptionalMagic<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int getConstructor() {
        return OPTIONAL_CONSTRUCTOR;
    }
}
