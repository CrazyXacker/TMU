package xyz.tmuapp.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class StringListAdapter implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return context.deserialize(json, typeOfT);
        } catch (Exception ignored) {
            // Maybe incorrect list
            return null;
        }
    }
}
