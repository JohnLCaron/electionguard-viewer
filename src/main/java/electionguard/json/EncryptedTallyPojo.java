package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.core.ElGamalCiphertext;
import electionguard.ballot.EncryptedTally;
import electionguard.core.ElementModP;
import electionguard.core.UInt256;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Conversion between CiphertextTally and Json, using python's object model. */
public class EncryptedTallyPojo {
  public String object_id;
  public Map<String, CiphertextTallyContestPojo> contests;

  public static class CiphertextTallyContestPojo {
    public String object_id;
    public int sequence_order; // JSON leaves it out when 0 ? or old versions dont have this
    public UInt256 description_hash;
    public Map<String, CiphertextTallySelectionPojo> selections;
  }

  public static class CiphertextTallySelectionPojo {
    public String object_id;
    public int sequence_order;
    public UInt256 description_hash;
    public CiphertextPojo ciphertext;
  }

  public static class CiphertextPojo {
    public ElementModP pad;
    public ElementModP data;
  }

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static EncryptedTally deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    EncryptedTallyPojo pojo = gson.fromJson(jsonElem, EncryptedTallyPojo.class);
    return translateTally(pojo);
  }

  private static EncryptedTally translateTally(EncryptedTallyPojo pojo) {
    List<EncryptedTally.Contest> contests = new ArrayList<>();
    for (Map.Entry<String, CiphertextTallyContestPojo> entry : pojo.contests.entrySet()) {
      contests.add(translateContest(entry.getValue()));
    }

    return new EncryptedTally(
            pojo.object_id,
            contests);
  }

  private static EncryptedTally.Contest translateContest(CiphertextTallyContestPojo pojo) {
    List<EncryptedTally.Selection> selections = new ArrayList<>();
    for (Map.Entry<String, CiphertextTallySelectionPojo> entry : pojo.selections.entrySet()) {
      selections.add(translateSelection(entry.getValue()));
    }
    return new EncryptedTally.Contest(
            pojo.object_id,
            pojo.sequence_order,
            pojo.description_hash,
            selections);
  }

  private static EncryptedTally.Selection translateSelection(CiphertextTallySelectionPojo pojo) {
    return new EncryptedTally.Selection(
            pojo.object_id,
            pojo.sequence_order,
            pojo.description_hash,
            translateCiphertext(pojo.ciphertext));
  }

  public static ElGamalCiphertext translateCiphertext(CiphertextPojo pojo) {
    return new ElGamalCiphertext(
            pojo.pad,
            pojo.data);
  }

  ////////////////////////////////////////////////////////////////////////////
  // serialize

  public static JsonElement serialize(EncryptedTally src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    EncryptedTallyPojo pojo = convertTally(src);
    Type typeOfSrc = new TypeToken<EncryptedTallyPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static EncryptedTallyPojo convertTally(EncryptedTally org) {
    Map<String, CiphertextTallyContestPojo> contests = new HashMap<>();
    for (EncryptedTally.Contest entry : org.getContests()) {
      contests.put(entry.getContestId(), convertContest(entry));
    }

    EncryptedTallyPojo pojo = new EncryptedTallyPojo();
    pojo.object_id = org.getTallyId();
    pojo.contests = contests;
    return pojo;
  }

  private static CiphertextTallyContestPojo convertContest(EncryptedTally.Contest org) {
    Map<String, CiphertextTallySelectionPojo> selections = new HashMap<>();
    for (EncryptedTally.Selection entry : org.getSelections()) {
      selections.put(entry.getSelectionId(), convertSelection(entry));
    }
    CiphertextTallyContestPojo pojo = new CiphertextTallyContestPojo();
    pojo.object_id = org.getContestId();
    pojo.sequence_order = org.getSequenceOrder();
    pojo.description_hash = org.getContestDescriptionHash();
    pojo.selections = selections;
    return pojo;
  }

  private static CiphertextTallySelectionPojo convertSelection(EncryptedTally.Selection org) {
    CiphertextTallySelectionPojo pojo = new CiphertextTallySelectionPojo();
    pojo.object_id = org.getSelectionId();
    pojo.sequence_order = org.getSequenceOrder();
    pojo.description_hash = org.getSelectionDescriptionHash();
    pojo.ciphertext = convertCiphertext(org.getCiphertext());
    return pojo;
  }

  public static CiphertextPojo convertCiphertext(ElGamalCiphertext org) {
    CiphertextPojo pojo = new CiphertextPojo();
    pojo.pad = org.getPad();
    pojo.data = org.getData();
    return pojo;
  }

}
