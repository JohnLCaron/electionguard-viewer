package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.ballot.PlaintextTally;
import electionguard.core.ElementModP;
import electionguard.core.ElementModQ;
import electionguard.core.GenericChaumPedersenProof;
import electionguard.decrypt.PartialDecryption;
import electionguard.decrypt.RecoveredPartialDecryption;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Conversion between PlaintextTally and Json, using python's object model. */
public class PlaintextTallyPojo {
  public String object_id;
  public Map<String, PlaintextTallyContestPojo> contests;

  public static class PlaintextTallyContestPojo {
    public String object_id;
    public Map<String, PlaintextTallySelectionPojo> selections;
  }

  public static class PlaintextTallySelectionPojo {
    public String object_id;
    public Integer tally;
    public ElementModP value;
    public EncryptedTallyPojo.CiphertextPojo message;
    public List<CiphertextDecryptionSelectionPojo> shares;
  }

  public static class CiphertextDecryptionSelectionPojo {
    public String object_id;
    public String guardian_id;
    public ElementModP share;
    public ChaumPedersenProofPojo proof; // Optional
    public Map<String, RecoveredPartialDecryptionPojo> recovered_parts; // Optional
  }

  public static class RecoveredPartialDecryptionPojo {
    public String object_id;
    public String guardian_id;
    public String missing_guardian_id;
    public ElementModP share;
    public ElementModP recovery_key;
    public ChaumPedersenProofPojo proof;
  }

  public static class ChaumPedersenProofPojo {
    public ElementModP pad;
    public ElementModP data;
    public ElementModQ challenge;
    public ElementModQ response;
  }

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static PlaintextTally deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextTallyPojo pojo = gson.fromJson(jsonElem, PlaintextTallyPojo.class);
    return translateTally(pojo);
  }

  private static PlaintextTally translateTally(PlaintextTallyPojo pojo) {
    Map<String, PlaintextTally.Contest> contests = pojo.contests.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e2 -> translateContest(e2.getValue())));

    return new PlaintextTally(
            pojo.object_id,
            contests);
  }

  private static PlaintextTally.Contest translateContest(PlaintextTallyContestPojo pojo) {
    Map<String, PlaintextTally.Selection> selections = pojo.selections.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e2 -> translateSelection(e2.getValue())));

    return new PlaintextTally.Contest(
            pojo.object_id,
            selections);
  }

  private static PlaintextTally.Selection translateSelection(PlaintextTallySelectionPojo pojo) {
    return new PlaintextTally.Selection(
            pojo.object_id,
            pojo.tally,
            pojo.value,
            EncryptedTallyPojo.translateCiphertext(pojo.message),
            ConvertPojos.convertList(pojo.shares, PlaintextTallyPojo::translateShare));
  }

  private static PartialDecryption translateShare(CiphertextDecryptionSelectionPojo pojo) {
    List<RecoveredPartialDecryption> recovered = null;
    if (pojo.recovered_parts != null) {
      recovered = new ArrayList<>();
      for (Map.Entry<String, RecoveredPartialDecryptionPojo> entry : pojo.recovered_parts.entrySet()) {
        recovered.add(translateCompensatedShare(entry.getValue()));
      }
    }

    return new PartialDecryption(
            pojo.object_id,
            pojo.guardian_id,
            pojo.share,
            translateProof(pojo.proof),
            recovered);
  }

  private static RecoveredPartialDecryption translateCompensatedShare(
          RecoveredPartialDecryptionPojo pojo) {

    return new RecoveredPartialDecryption(
            pojo.guardian_id,
            pojo.missing_guardian_id,
            pojo.share,
            pojo.recovery_key,
            translateProof(pojo.proof));
  }


  @Nullable
  private static GenericChaumPedersenProof translateProof(@Nullable PlaintextTallyPojo.ChaumPedersenProofPojo proof) {
    if (proof == null) {
      return null;
    }
    return new GenericChaumPedersenProof(
            proof.challenge,
            proof.response);
  }

  ////////////////////////////////////////////////////////////////////////////
  // serialize

  public static JsonElement serialize(PlaintextTally src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    PlaintextTallyPojo pojo = convertTally(src);
    Type typeOfSrc = new TypeToken<PlaintextTallyPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static PlaintextTallyPojo convertTally(PlaintextTally org) {
    PlaintextTallyPojo pojo = new PlaintextTallyPojo();
    pojo.object_id = org.getTallyId();

    pojo.contests = org.getContests().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e2 -> convertContest(e2.getValue())));

    return pojo;
  }

  private static PlaintextTallyContestPojo convertContest(PlaintextTally.Contest org) {
    PlaintextTallyContestPojo pojo = new PlaintextTallyContestPojo();
    pojo.object_id = org.getContestId();
    pojo.selections = org.getSelections().entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e2 -> convertSelection(e2.getValue())));
    return pojo;
  }

  private static PlaintextTallySelectionPojo convertSelection(PlaintextTally.Selection org) {
    PlaintextTallySelectionPojo pojo = new PlaintextTallySelectionPojo();
    pojo.object_id = org.getSelectionId();
    pojo.tally = org.getTally();
    pojo.value = org.getValue();
    pojo.message = EncryptedTallyPojo.convertCiphertext(org.getMessage());
    pojo.shares = ConvertPojos.convertList(org.getPartialDecryptions(), PlaintextTallyPojo::convertShare);
    return pojo;
  }

  private static CiphertextDecryptionSelectionPojo convertShare(PartialDecryption org) {
    final Map<String, RecoveredPartialDecryptionPojo> recovered = new HashMap<>();
    if (!org.getRecoveredDecryptions().isEmpty()) {
      for (RecoveredPartialDecryption entry : org.getRecoveredDecryptions()) {
        recovered.put(entry.getDecryptingGuardianId(), convertCompensatedShare(org.getSelectionId(), entry));
      }
    }

    CiphertextDecryptionSelectionPojo pojo = new CiphertextDecryptionSelectionPojo();
    pojo.object_id = org.getSelectionId();
    pojo.guardian_id = org.getGuardianId();
    pojo.share = org.share();
    if (org.getProof() != null) {
      pojo.proof = convertProof(org.getProof());
    } else {
      pojo.recovered_parts = recovered;
    }
    return pojo;
  }

  private static RecoveredPartialDecryptionPojo convertCompensatedShare(
          String selectionId, RecoveredPartialDecryption org) {

    RecoveredPartialDecryptionPojo pojo = new RecoveredPartialDecryptionPojo();
    pojo.object_id = selectionId;
    pojo.guardian_id = org.getDecryptingGuardianId();
    pojo.missing_guardian_id = org.getMissingGuardianId();
    pojo.share = org.getShare();
    pojo.recovery_key = org.getRecoveryKey();
    pojo.proof = convertProof(org.getProof());
    return pojo;
  }

  private static ChaumPedersenProofPojo convertProof(GenericChaumPedersenProof proof) {
    ChaumPedersenProofPojo pojo = new ChaumPedersenProofPojo();
    pojo.pad = null; // LOOK
    pojo.data = null; // LOOK
    pojo.challenge = proof.getC();
    pojo.response = proof.getR();
    return pojo;
  }

}
