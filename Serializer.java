package com.bobsystem.exercise.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);

    //region static methods
    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream()) {
            ObjectOutputStream objOutStream = new ObjectOutputStream(bytesOutStream);
            objOutStream.writeObject(obj);
            return bytesOutStream.toByteArray();
        }
        catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T unserizlize(byte[] bytes) {
        try (ObjectInputStream objInStream =
                 new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T)objInStream.readObject();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
    //endregion

    //region constructors
    private Serializer() { }
    //endregion
}
