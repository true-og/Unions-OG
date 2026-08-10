package net.trueog.unionsog.proxy.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnionPlayerListAdapter extends TypeAdapter<List<UnionPlayer>> {

    private final UnionsOG plugin;

    public UnionPlayerListAdapter(UnionsOG plugin) {

        this.plugin = plugin;

    }

    @Override
    public void write(JsonWriter out, List<UnionPlayer> value) throws IOException {

        out.beginArray();
        for (UnionPlayer unionPlayer : value) {

            out.beginObject();
            out.name("uuid");
            out.value(unionPlayer.getUniqueId().toString());
            out.endObject();

        }

        out.endArray();

    }

    @Override
    public List<UnionPlayer> read(JsonReader in) throws IOException {

        List<UnionPlayer> list = new ArrayList<>();
        in.beginArray();
        while (in.peek() == JsonToken.BEGIN_OBJECT) {

            in.beginObject();
            in.nextName();
            UUID uuid = UUID.fromString(in.nextString());
            UnionPlayer cp = plugin.getUnionManager().getAnyUnionPlayer(uuid);
            in.endObject();
            if (cp != null) {

                list.add(cp);

            }

        }

        in.endArray();
        return list;

    }

    public static Type getType() {

        return TypeToken.getParameterized(List.class, UnionPlayer.class).getType();

    }

}
