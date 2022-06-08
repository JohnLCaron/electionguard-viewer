package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.ballot.DecryptingGuardian;
import electionguard.core.ElementModQ;

import java.lang.reflect.Type;

/** Conversion between DecryptingGuardian and Json, because records can be instantiated by reflection */
public class DecryptingGuardianPojo {
  public String guardian_id;
  public Integer sequence;
  public ElementModQ lagrangeCoordinate;

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static DecryptingGuardian deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    DecryptingGuardianPojo pojo = gson.fromJson(jsonElem, DecryptingGuardianPojo.class);
    return translateAvailableGuardian(pojo);
  }

  private static DecryptingGuardian translateAvailableGuardian(DecryptingGuardianPojo pojo) {
    return new DecryptingGuardian(
            pojo.guardian_id,
            pojo.sequence,
            pojo.lagrangeCoordinate);
  }

  ////////////////////////////////////////////////////////////////////////////
  // serialize

  public static JsonElement serialize(DecryptingGuardian src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    DecryptingGuardianPojo pojo = convertAvailableGuardian(src);
    Type typeOfSrc = new TypeToken<DecryptingGuardianPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static DecryptingGuardianPojo convertAvailableGuardian(DecryptingGuardian org) {
    DecryptingGuardianPojo pojo = new DecryptingGuardianPojo();
    pojo.guardian_id = org.getGuardianId();
    pojo.sequence = org.getXCoordinate();
    pojo.lagrangeCoordinate = org.getLagrangeCoordinate();
    return pojo;
  }

}
