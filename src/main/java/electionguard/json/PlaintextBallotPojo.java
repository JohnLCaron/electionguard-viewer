package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.ballot.PlaintextBallot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Conversion between PlaintextBallot and Json, using python's object model. */
public class PlaintextBallotPojo {
  public String object_id;
  public String style_id;
  public List<PlaintextBallotContest> contests;

  public static class PlaintextBallotContest {
    public String object_id;
    public Integer sequence_order;
    public List<PlaintextBallotSelection> ballot_selections;
  }

  public static class PlaintextBallotSelection {
    public String object_id;
    public Integer sequence_order;
    public Integer vote;
    public ExtendedData extended_data;
  }

  public static class ExtendedData {
    public String value;
    public Integer length;

    public ExtendedData(String value, Integer length) {
      this.value = value;
      this.length = length;
    }
  }

  /////////////////////////////////////

  public static List<PlaintextBallot> get_ballots_from_file(String filename) throws IOException {
    try (InputStream is = new FileInputStream(filename)) {
      Reader reader = new InputStreamReader(is);
      Gson gson = GsonTypeAdapters.enhancedGson();
      Type listType = new TypeToken<ArrayList<PlaintextBallotPojo>>(){}.getType();

      List<PlaintextBallotPojo> pojo = gson.fromJson(reader, listType);
      return ConvertPojos.convertList(pojo, PlaintextBallotPojo::convertPlaintextBallot);
    }
  }

  public static PlaintextBallot get_ballot_from_file(String filename) throws IOException {
    try (InputStream is = new FileInputStream(filename)) {
      Reader reader = new InputStreamReader(is);
      Gson gson = GsonTypeAdapters.enhancedGson();
      PlaintextBallotPojo pojo = gson.fromJson(reader, PlaintextBallotPojo.class);
      return convertPlaintextBallot(pojo);
    }
  }

  ///////////////////////////////////////////////////////////////////////////////////////

  public static PlaintextBallot deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextBallotPojo pojo = gson.fromJson(jsonElem, PlaintextBallotPojo.class);
    return convertPlaintextBallot(pojo);
  }

  private static PlaintextBallot convertPlaintextBallot(PlaintextBallotPojo pojo) {
    return new PlaintextBallot(
            pojo.object_id,
            pojo.style_id,
            ConvertPojos.convertList(pojo.contests, PlaintextBallotPojo::convertPlaintextBallotContest),
            null);
  }

  private static PlaintextBallot.Contest convertPlaintextBallotContest(PlaintextBallotContest pojo) {
    return new PlaintextBallot.Contest(
            pojo.object_id,
            pojo.sequence_order,
            ConvertPojos.convertList(pojo.ballot_selections, PlaintextBallotPojo::convertPlaintextBallotSelection));
  }

  private static PlaintextBallot.Selection convertPlaintextBallotSelection(PlaintextBallotSelection pojo) {
    String extendedData = pojo.extended_data != null ? pojo.extended_data.value : null;
    return new PlaintextBallot.Selection(
            pojo.object_id,
            pojo.sequence_order,
            pojo.vote,
            extendedData);
  }

  ////////////////////////////////////////////////////////////////////////////////////////

  public static JsonElement serialize(PlaintextBallot src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextBallotPojo pojo = convertPlaintextBallot(src);
    Type typeOfSrc = new TypeToken<PlaintextBallotPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static PlaintextBallotPojo convertPlaintextBallot(PlaintextBallot src) {
     PlaintextBallotPojo pojo = new PlaintextBallotPojo();
    pojo.object_id = src.getBallotId();
    pojo.style_id = src.getBallotStyleId();
    pojo.contests = ConvertPojos.convertList(src.getContests(), PlaintextBallotPojo::convertPlaintextBallotContest);
    return pojo;
  }

  private static PlaintextBallotContest convertPlaintextBallotContest(PlaintextBallot.Contest src) {
    PlaintextBallotContest pojo = new PlaintextBallotContest ();
    pojo.object_id = src.getContestId();
    pojo.sequence_order = src.getSequenceOrder();
    pojo.ballot_selections = ConvertPojos.convertList(src.getSelections(), PlaintextBallotPojo::convertPlaintextBallotSelection);
    return pojo;
  }

  private static PlaintextBallotSelection convertPlaintextBallotSelection(PlaintextBallot.Selection src) {
    PlaintextBallotSelection pojo = new PlaintextBallotSelection ();
    pojo.object_id = src.getSelectionId();
    pojo.sequence_order = src.getSequenceOrder();
    pojo.vote = src.getVote();
    if (src.getExtendedData() != null) {
      pojo.extended_data = new ExtendedData(src.getExtendedData(), src.getExtendedData().length());
    }
    return pojo;
  }

}
