package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.ballot.Guardian;
import electionguard.core.SchnorrProof;
import electionguard.core.ElementModP;
import electionguard.core.ElementModQ;

import java.lang.reflect.Type;
import java.util.List;

/** Conversion between GuardianRecord and Json, using python's object model. */
public class GuardianRecordPojo {
  public String guardian_id;
  public int sequence_order;
  public ElementModP election_public_key;
  public List<ElementModP> election_commitments;
  public List<SchnorrProofPojo> election_proofs;

  public static class SchnorrProofPojo {
    public ElementModP public_key;
    public ElementModP commitment;
    public ElementModQ challenge;
    public ElementModQ response;
  }

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static Guardian deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    GuardianRecordPojo pojo = gson.fromJson(jsonElem, GuardianRecordPojo.class);
    return translateGuardianRecord(pojo);
  }

  private static Guardian translateGuardianRecord(GuardianRecordPojo pojo) {
    return new Guardian(
            pojo.guardian_id,
            pojo.sequence_order,
            pojo.election_commitments,
            ConvertPojos.convertList(pojo.election_proofs, GuardianRecordPojo::translateProof));
  }

  private static SchnorrProof translateProof(SchnorrProofPojo pojo) {
    return new SchnorrProof(
            pojo.challenge,
            pojo.response);
  }

  ////////////////////////////////////////////////////////////////////////////
  // serialize

  public static JsonElement serialize(Guardian src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    GuardianRecordPojo pojo = convertCoefficients(src);
    Type typeOfSrc = new TypeToken<GuardianRecordPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static GuardianRecordPojo convertCoefficients(Guardian org) {
    GuardianRecordPojo pojo = new GuardianRecordPojo();
    pojo.guardian_id = org.getGuardianId();
    pojo.sequence_order = org.getXCoordinate();
    pojo.election_public_key = org.publicKey();
    pojo.election_commitments = org.getCoefficientCommitments();
    pojo.election_proofs = ConvertPojos.convertList(org.getCoefficientProofs(), GuardianRecordPojo::convertProof);
    return pojo;
  }

  private static SchnorrProofPojo convertProof(SchnorrProof org) {
    SchnorrProofPojo pojo = new SchnorrProofPojo();
    pojo.public_key = null; // LOOK
    pojo.commitment = null; // LOOK
    pojo.challenge  = org.getChallenge();
    pojo.response = org.getResponse();
    return pojo;
  }

}
