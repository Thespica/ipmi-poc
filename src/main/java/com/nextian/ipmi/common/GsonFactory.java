/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */

package com.nextian.ipmi.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Type;

/**
 * GSon factory to create Gson serializer. Additional HEX serialization handler is registered. Sucj implementation
 * allow to serialize messages containing data (byte array).
 * Class is supposed to be used for message tracing
 */
public class GsonFactory {

    /**
     * Hide default constructor
     */
    private GsonFactory() {
    }

    /**
     Singleton object reference
     */
    private static Gson gson = null;

    /**
     * Get gson object to be used for JSON serialization. Method handle singleton implementation
     *
     * @return json singleton object
     */
    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new HexTypeAdapter()).create();
        }
        return gson;
    }

    /**
     * Class implements more user friendly representation on byte array used in GSON library. It can encode/decode
     * byte array to hex string.
     */
    private static class HexTypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return DatatypeConverter.parseHexBinary(json.getAsString());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(e);
            }
        }

        public JsonElement serialize(byte[] data, Type typeOfSrc, JsonSerializationContext context) {
            String msg = DatatypeConverter.printHexBinary(data);
            return new JsonPrimitive(msg);
        }
    }

}
