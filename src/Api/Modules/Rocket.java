package Api.Modules;

import Api.ApiMiner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public record Rocket(double height, double diameter, int mass, String name, boolean active, int stages, int boosters,
                     JSONArray payloadWights, int costPerLaunch, Calendar firstFlight, String id) {

    public static Rocket initRocket(String id) throws IOException {
        JSONObject rocket = ApiMiner.sendRequest("/v4/rockets/" + id);
        Integer[] date = Arrays.stream(rocket.getString("first_flight").
                split("-")).map(Integer::parseInt).toArray(Integer[]::new);
        Calendar calendar = Calendar.getInstance();
        calendar.set(date[0], date[1], date[2]);
        return new Rocket(rocket.getJSONObject("height").optDouble("meters", 0),
                rocket.getJSONObject("diameter").optDouble("meters", 0),
                rocket.getJSONObject("mass").optInt("kg", 0),
                rocket.getString("name"),
                rocket.getBoolean("active"),
                rocket.getInt("stages"),
                rocket.getInt("boosters"),
                rocket.getJSONArray("payload_weights"),
                rocket.getInt("cost_per_launch"),
                calendar,
                rocket.getString("id"));
    }
}
