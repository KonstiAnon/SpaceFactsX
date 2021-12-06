package Api;

import Api.Modules.Launch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.CollectionsHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ApiMiner {
    //The URL which is used for all requests
    public static final String baseURL = "https://api.spacexdata.com";

    //Scanner object to retrieve user input
    Scanner scanner;

    /**
     * This function is used for finding out facts about launches.
     * This includes:
     * [1] Total cost
     * [2] Specific launch
     * [3] Calculating the success rate
     * [4] Information about the next launch
     */
    public void launchFacts() {
        System.out.println("Welcome to the launch facts category. What do you wish to find out more about?");
        System.out.println("\t[1]Total cost of all launches");
        System.out.println("\t[2]I wish to find out more about one specific launch");
        System.out.println("\t[3]What is the success rate of all launches?");
        System.out.println("\t[4]Tell me about the next launch!");
        System.out.println("\t[0]Back to the main menu");

        scanner = new Scanner(System.in);
        switch (scanner.nextLine()) {
            case "1" -> {
                try {
                    System.out.println("Calculating the total cost of all launches....");
                    //Every launch is counted as it is not specified whether the launch cost applies to all launches or only successful ones
                    HashMap<String, Integer> idToCost = new HashMap<>();
                    JSONArray arrayOfLaunches = arrayRequest("/v5/launches");
                    JSONArray arrayOfRockets = arrayRequest("/v4/rockets");
                    long sum = 0;
                    JSONObject object;

                    //Looping through all rocket-(type)s that have every been used
                    for (Object obj : arrayOfRockets) {
                        idToCost.put(((JSONObject) obj).getString("id"), ((JSONObject) obj).getInt("cost_per_launch"));
                    }
                    //Adding the cost of a specific rocket to the total
                    for (int i = 0; i < arrayOfLaunches.length(); i++) {
                        object = arrayOfLaunches.getJSONObject(i);
                        sum += idToCost.get(object.getString("rocket"));
                    }
                    System.out.println("The approximate total cost of all launches is: $" + sum);
                } catch (IOException e) {
                    System.out.println("There was an error executing your command. Sorry :(");
                }
            }
            case "2" -> {
                System.out.println("Which launch do you wish to find out more about? (Please enter a number)");
                JSONArray listOfLaunches;
                int flightNr = scanner.nextInt();
                try {
                        /*
                        Since the API only allows for requests using an id I had to retrieve all launches and then get the one wanted
                        The API also contains a small error. Although there only being 147 elements the last one has number 152.
                        Thus the result of this function does not always return the launch with number n but instead the n-th element
                        in the array
                         */

                    listOfLaunches = arrayRequest("/v5/launches");

                    //Error handling
                    if (flightNr > listOfLaunches.length() || flightNr <= 0) {
                        System.out.println("Number not within 1 and " + listOfLaunches.length());
                        throw new IOException();
                    }

                    //Printing out information about the n-th element in the array
                    JSONObject launchObject = listOfLaunches.getJSONObject(flightNr - 1);
                    Launch launch = Launch.initLaunch(launchObject.getString("id"));
                    System.out.println(launch);
                } catch (IOException | JSONException e) {
                    System.out.println("There was an error handling your request");
                    e.printStackTrace();
                }
            }
            case "3" -> {
                try {
                    double successRate = 0;
                    int countedElements = 0;
                    JSONArray launches = arrayRequest("/v5/launches");
                    for (Object obj : launches) {
                        //Ignoring future launches
                        if (!((JSONObject) obj).getBoolean("upcoming")) {
                            countedElements++;
                            successRate += (((JSONObject) obj).getBoolean("success")) ? 100 : 0;
                        }
                    }
                    //To prevent division by zero
                    if (countedElements != 0) {
                        System.out.println("The calculated success rate of all previous launches is: " + Math.floor((successRate / countedElements) * 100) / 100 + "%");
                    } else {
                        System.out.println("The was an error fetching the data!");
                    }
                } catch (IOException e) {
                    System.out.println("There was an error executing your request");
                }
            }
            case "4" -> {
                Launch launch;
                try {
                    launch = Launch.initLaunch("next");
                    System.out.println(launch);
                } catch (IOException e) {
                    System.out.println("Could not get information about the next launch.");
                }
            }
            case "0" -> {
                //Do nothing, automatically returns back
            }
            default -> System.out.println("Please enter a number between 0 and 4");

        }

    }

    /**
     * This function is used for finding out facts about astronauts
     */
    public void astronautFacts() {
        System.out.println("Welcome to the astronauts category! What do you wish to find out more about?");
        System.out.println("\t[1]Find out which agency most crew members work for");
        System.out.println("\t[2]Find out which crew member spent the most time in space");
        System.out.println("\t[0]Return to main menu");
        try {
            scanner = new Scanner(System.in);
            switch (scanner.nextLine()) {
                case "1" -> {
                    JSONArray crew = arrayRequest("/v4/crew");
                    Map<String, Integer> agencies = new HashMap<>();

                    //Filling a Hashmap with the agencies and how many crew members they have supplied
                    for (Object member : crew) {
                        agencies.put(((JSONObject) member).getString("agency"),
                                (agencies.containsKey(((JSONObject) member).getString("agency")))
                                        ? agencies.get(((JSONObject) member).getString("agency")) + 1 : 1);
                    }
                    Map<String, Integer> sorted = CollectionsHelper.sortDescending(agencies);
                    sorted.forEach((s, integer) -> System.out.println(s + " has supplied: " + integer + " crew members."));
                }
                case "2" -> {

                    //The way this function works is, for every crew member there is it looks at the launches and them sums the time the payloads spent in space
                    //This function assumes that every crew member leaves with the same capsule they reached the space station with
                    System.out.println("------------longest time spent in space------------");
                    JSONArray crew = arrayRequest("/v4/crew");
                    Map<String, Long> timeInSpace = new HashMap<>();
                    //Getting all launches at once to make it more efficient (and not get banned from the API)
                    JSONArray allLaunches = arrayRequest("/v5/launches");
                    Map<String, Integer> launchToTime = new HashMap<>();
                    //Preparing a Map which lists the time spent in space for each launch, this helps increase efficiency
                    for (Object obj : allLaunches) {
                        if (((JSONObject) obj).getJSONArray("crew").length() != 0) {
                            JSONObject payload = sendRequest("/v4/payloads/" + ((JSONObject) obj).getJSONArray("payloads").get(0));
                            launchToTime.put(((JSONObject) obj).getString("id"), payload.getJSONObject("dragon").optInt("flight_time_sec", 0));
                        }
                    }
                    for (Object member : crew) {
                        //Get the launches one crew member partook in
                        JSONArray launches = ((JSONObject) member).getJSONArray("launches");
                        //Loop though all launches, currently no crew member has more than one launch, I still choose to keep it like this
                        for (Object launch : launches) {
                            long timeInSec = launchToTime.getOrDefault(launch, 0);
                            String name = ((JSONObject) member).getString("name");
                            timeInSpace.put(name, timeInSpace.containsKey(name) ? timeInSpace.get(name) + timeInSec : timeInSec);
                        }
                    }
                    Map<String, Long> sorted = CollectionsHelper.sortDescendingLong(timeInSpace);
                    sorted.forEach((s, aLong) -> {
                        //Astronauts currently in space return null as a value, we ignore them
                        if (aLong != 0) {
                            System.out.println(s + " has spent a total of: " + (aLong / 60) / 60 + " hours in space.");
                        }
                    });
                    System.out.println("------------longest time spent in space------------");
                }
                default -> System.out.println("Please enter a number between 1 and 2");
            }
        } catch (IOException e) {
            //Ignore
        }
    }

    /**
     * Returns a JSON Object of any Module. Useful for specific requests.
     *
     * @param request This is the id or command (for launches it can be next for example) that is used to retrieve
     *                the data from the API
     * @return Returns an JSON Object containing all data from the API request
     * @throws IOException
     */
    public static JSONObject sendRequest(String request) throws IOException {
        BufferedReader input;
        URL url = new URL(baseURL + request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = input.readLine();
        return new JSONObject(response);
    }

    /**
     * Returns an array of JSONObjects. This is useful for requests that don't specify an specific element,
     * such as for example the request to get all launches
     *
     * @param request This is the path used for the request
     * @return An array of JSONObjects
     * @throws IOException
     */
    public static JSONArray arrayRequest(String request) throws IOException {
        BufferedReader input;
        URL url = new URL(baseURL + request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = input.readLine();
        return new JSONArray(response);
    }
}
