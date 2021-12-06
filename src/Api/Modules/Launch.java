package Api.Modules;

import Api.ApiMiner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public record Launch(Date launchDate, String rocketId, boolean success, JSONArray capsules, JSONArray crew,
                     String launchpad, boolean upcoming, JSONArray cores, String id) {

    public String toString() {
        Rocket rocket;
        try {
            rocket = Rocket.initRocket(rocketId);
        }catch (IOException e){
            rocket = null;
        }
        StringBuilder str = new StringBuilder();
        str.append("-------------------------\n");
        str.append("Rocket Facts\n");
        str.append("Finding out more about Rocket with ID: " + id + "\n");
        str.append("The rocket " + (upcoming ? "is going to launch on the: " : "launched on the: ") + launchDate.toString() + " UTC\n");
        if (!upcoming) {
            str.append("The mission was considered " + (success ? "successful" : "not successful") + "\n");
        }
        str.append("The Mission is " + (crew.length() != 0 ? "manned and the crew consisted of " + crew.length() + " people" : "unmanned") + "\n");
        str.append("The amount of cores used was: " + cores.length() + "\n");
        if (rocket != null){
            str.append("The rocket used is of type: " + rocket.name() + "\n");
            str.append("The costs of launching the rocket are approximately: $" + rocket.costPerLaunch() + "\n");
            str.append("To find out more about the rocket used, here's the ID: " + rocket.id() + "\n");
        }
        str.append("-------------------------\n");
        return str.toString();
    }

    public static Launch initLaunch(String id) throws IOException {
        JSONObject launchObject = ApiMiner.sendRequest("/v5/launches/" + id);
        return new Launch(new Date(launchObject.getLong("date_unix") * 1000),
                launchObject.getString("rocket"),
                launchObject.optBoolean("success",false),
                launchObject.getJSONArray("capsules"),
                launchObject.getJSONArray("crew"),
                launchObject.getString("launchpad"),
                launchObject.getBoolean("upcoming"),
                launchObject.getJSONArray("cores"),
                launchObject.getString("id"));
    }

}
