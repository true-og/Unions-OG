package net.trueog.unionsog.proxy.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.trueog.unionsog.Clan;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.Nullable;

public class ClanPlayerTypeAdapterFactory implements TypeAdapterFactory {

    private final UnionsOG plugin;

    public ClanPlayerTypeAdapterFactory(UnionsOG plugin) {

        this.plugin = plugin;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

        if (type.getType() != ClanPlayer.class) {

            return null;

        }

        final TypeAdapter<ClanPlayer> delegate = (TypeAdapter<ClanPlayer>) gson.getDelegateAdapter(this, type);

        return (TypeAdapter<T>) new TypeAdapter<ClanPlayer>() {

            @Override
            public void write(JsonWriter out, ClanPlayer value) {

                JsonObject object = delegate.toJsonTree(value).getAsJsonObject();
                object.addProperty("clan", value.getTag());
                gson.toJson(object, out);

            }

            @Override
            public ClanPlayer read(JsonReader in) {

                JsonObject object = gson.fromJson(in, JsonObject.class);
                String tag = object.get("clan").getAsString();
                Clan clan = plugin.getClanManager().getClan(tag);
                object.add("clan", null);

                ClanPlayer cp = delegate.fromJsonTree(object);
                cp.setClan(clan);
                return cp;

            }

        };

    }

}
