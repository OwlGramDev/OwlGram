package it.owlgram.android.magic;


import java.util.NoSuchElementException;
import java.util.Objects;

public class OptionalMagic<T> {
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
        try {
            if (object instanceof MagicBaseObject) {
                ((MagicBaseObject) object).readParams(stream, true);
            } else {
                throw new RuntimeException("Object not MagicBaseObject");
            }
            return new OptionalMagic<>(object);
        } catch (RuntimeException ignored) {}
        return null;
    }

    public byte[] serializeToStream() {
        if (isPresent() && value instanceof MagicBaseObject) {
            return ((MagicBaseObject) value).serializeToStream();
        }
        return null;
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

}
