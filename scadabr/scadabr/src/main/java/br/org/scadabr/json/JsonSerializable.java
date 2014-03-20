package br.org.scadabr.json;

import java.util.Map;

public abstract interface JsonSerializable {

    public abstract void jsonSerialize(Map<String, Object> paramMap);

    public abstract void jsonDeserialize(JsonReader paramJsonReader, JsonObject paramJsonObject)
            throws JsonException;
}
