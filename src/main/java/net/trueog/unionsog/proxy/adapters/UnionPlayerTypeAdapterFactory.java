package net.trueog.unionsog.proxy.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.Nullable;

public class UnionPlayerTypeAdapterFactory implements TypeAdapterFactory {

    private final UnionsOG plugin;

    public UnionPlayerTypeAdapterFactory(UnionsOG plugin) {

        this.plugin = plugin;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

        if (type.getType() != UnionPlayer.class) {

            return null;

        }

        final TypeAdapter<UnionPlayer> delegate = (TypeAdapter<UnionPlayer>) gson.getDelegateAdapter(this, type);

        return (TypeAdapter<T>) new TypeAdapter<UnionPlayer>() {

            @Override
            public void write(JsonWriter out, UnionPlayer value) {

                JsonObject object = delegate.toJsonTree(value).getAsJsonObject();
                object.addProperty("union", value.getTag());
                gson.toJson(object, out);

            }

            @Override
            public UnionPlayer read(JsonReader in) {

                JsonObject object = gson.fromJson(in, JsonObject.class);
                String tag = object.get("union").getAsString();
                Union union = plugin.getUnionManager().getUnion(tag);
                object.add("union", null);

                UnionPlayer cp = delegate.fromJsonTree(object);
                cp.setUnion(union);
                return cp;

            }

        };

    }

}
