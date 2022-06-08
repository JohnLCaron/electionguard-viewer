package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import electionguard.ballot.EncryptedBallot;
import electionguard.core.ConstantChaumPedersenProofKnownNonce;
import electionguard.core.DisjunctiveChaumPedersenProofKnownNonce;
import electionguard.core.ElGamalCiphertext;
import electionguard.core.ElementModP;
import electionguard.core.ElementModQ;
import electionguard.core.GenericChaumPedersenProof;
import electionguard.core.UInt256;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

/** Conversion between SubmittedBallot and Json, using python's object model. */
public class EncryptedBallotPojo {
  public String object_id;
  public String style_id;
  public UInt256 manifest_hash;
  public UInt256 code;
  public UInt256 code_seed;
  public List<EncryptedBallotContestPojo> contests;
  public Long timestamp;
  public UInt256 crypto_hash;
  public ElementModQ nonce; // LOOK nonce always must be null, so can omit?
  public EncryptedBallot.BallotState state;

  public static class EncryptedBallotContestPojo {
    public String object_id;
    public Integer sequence_order;
    public UInt256 description_hash;
    public List<EncryptedBallotSelectionPojo> ballot_selections;
    public UInt256 crypto_hash;
    public ElementModQ nonce; // LOOK nonce always must be null, so can omit?
    public ConstantChaumPedersenProofPojo proof;
  }

  public static class EncryptedBallotSelectionPojo {
    public String object_id;
    public Integer sequence_order;
    public UInt256 description_hash;
    public ElGamalCiphertextPojo ciphertext;
    public UInt256 crypto_hash;
    public Boolean is_placeholder_selection = Boolean.FALSE;
    public ElementModQ nonce; // LOOK nonce always must be null, so can omit?
    public DisjunctiveChaumPedersenProofPojo proof;
    public ElGamalCiphertextPojo extended_data;
  }

  public static class ConstantChaumPedersenProofPojo {
    public ElementModP pad;
    public ElementModP data;
    public ElementModQ challenge;
    public ElementModQ response;
    public Integer constant;
  }

  public static class DisjunctiveChaumPedersenProofPojo {
    public ElementModP proof_zero_pad;
    public ElementModP proof_zero_data;
    public ElementModP proof_one_pad;
    public ElementModP proof_one_data;
    public ElementModQ proof_zero_challenge;
    public ElementModQ proof_one_challenge;
    public ElementModQ challenge;
    public ElementModQ proof_zero_response;
    public ElementModQ proof_one_response;
  }

  public static class ElGamalCiphertextPojo {
    public ElementModP pad;
    public ElementModP data;
  }

  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static EncryptedBallot deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    EncryptedBallotPojo pojo = gson.fromJson(jsonElem, EncryptedBallotPojo.class);
    return translateBallot(pojo);
  }

  private static EncryptedBallot translateBallot(EncryptedBallotPojo pojo) {
    return new EncryptedBallot(
            pojo.object_id,
            pojo.style_id,
            pojo.manifest_hash,
            pojo.code_seed,
            pojo.code,
            ConvertPojos.convertList(pojo.contests, EncryptedBallotPojo::translateContest),
            pojo.timestamp,
            pojo.crypto_hash,
            pojo.state);
  }

  private static EncryptedBallot.Contest translateContest(EncryptedBallotContestPojo contest) {
    return new EncryptedBallot.Contest(
            contest.object_id,
            contest.sequence_order,
            contest.description_hash,
            ConvertPojos.convertList(contest.ballot_selections, EncryptedBallotPojo::translateSelection),
            contest.crypto_hash,
            translateConstantProof(contest.proof));
  }

  private static EncryptedBallot.Selection translateSelection(EncryptedBallotSelectionPojo selection) {
    return new EncryptedBallot.Selection(
            selection.object_id,
            selection.sequence_order,
            selection.description_hash,
            translateCiphertext(selection.ciphertext),
            selection.crypto_hash,
            selection.is_placeholder_selection,
            translateDisjunctiveProof(selection.proof),
            null);
  }

  @Nullable
  private static ElGamalCiphertext translateCiphertext(@Nullable ElGamalCiphertextPojo ciphertext) {
    if (ciphertext == null) {
      return null;
    }
    return new ElGamalCiphertext(
            ciphertext.pad,
            ciphertext.data);
  }

  @Nullable
  private static ConstantChaumPedersenProofKnownNonce translateConstantProof(@Nullable ConstantChaumPedersenProofPojo proof) {
    if (proof == null) {
      return null;
    }

    GenericChaumPedersenProof gproof = new GenericChaumPedersenProof(
            proof.challenge,
            proof.response);

    return new ConstantChaumPedersenProofKnownNonce(
            gproof,
            proof.constant);
  }

  @Nullable
  private static DisjunctiveChaumPedersenProofKnownNonce translateDisjunctiveProof(@Nullable DisjunctiveChaumPedersenProofPojo proof) {
    if (proof == null) {
      return null;
    }

    GenericChaumPedersenProof proof0 = new GenericChaumPedersenProof(
            proof.proof_zero_challenge,
            proof.proof_zero_response);

    GenericChaumPedersenProof proof1 = new GenericChaumPedersenProof(
            proof.proof_one_challenge,
            proof.proof_one_response);

    return new DisjunctiveChaumPedersenProofKnownNonce(
            proof0,
            proof1,
            proof.challenge);
  }

  ////////////////////////////////////////////////////////////////////////////
  // serialize

  public static JsonElement serialize(EncryptedBallot src) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    EncryptedBallotPojo pojo = convertSubmittedBallot(src);
    Type typeOfSrc = new TypeToken<EncryptedBallotPojo>() {}.getType();
    return gson.toJsonTree(pojo, typeOfSrc);
  }

  private static EncryptedBallotPojo convertSubmittedBallot(EncryptedBallot org) {
    EncryptedBallotPojo pojo = new EncryptedBallotPojo();
    pojo.object_id = org.getBallotId();
    pojo.style_id = org.getBallotStyleId();
    pojo.manifest_hash = org.getManifestHash();
    pojo.code_seed = org.getCodeSeed();
    pojo.contests = ConvertPojos.convertList(org.getContests(), EncryptedBallotPojo::convertContest);
    pojo.code = org.getCode();
    pojo.timestamp = org.getTimestamp();
    pojo.crypto_hash = org.getCryptoHash();
    pojo.nonce = null;
    pojo.state = org.getState();
    return pojo;
  }

  private static EncryptedBallotContestPojo convertContest(EncryptedBallot.Contest contest) {
    EncryptedBallotContestPojo pojo = new EncryptedBallotContestPojo();
    pojo.object_id = contest.getContestId();
    pojo.sequence_order = contest.getSequenceOrder();
    pojo.description_hash = contest.getContestHash();
    pojo.ballot_selections = ConvertPojos.convertList(contest.getSelections(), EncryptedBallotPojo::convertSelection);
    pojo.crypto_hash = contest.getCryptoHash();
    pojo.nonce = null;
    pojo.proof = convertConstantProof(contest.getProof());
    return pojo;
  }

  private static EncryptedBallotSelectionPojo convertSelection(EncryptedBallot.Selection selection) {
    EncryptedBallotSelectionPojo pojo = new EncryptedBallotSelectionPojo();
    pojo.object_id = selection.getSelectionId();
    pojo.sequence_order = selection.getSequenceOrder();
    pojo.description_hash = selection.getSelectionHash();
    pojo.ciphertext = convertCiphertext(selection.getCiphertext());
    pojo.crypto_hash = selection.getCryptoHash();
    pojo.is_placeholder_selection = selection.isPlaceholderSelection();
    pojo.nonce = null;
    pojo.proof = convertDisjunctiveProof(selection.getProof());
    return pojo;
  }

  private static ElGamalCiphertextPojo convertCiphertext(ElGamalCiphertext ciphertext) {
    ElGamalCiphertextPojo pojo = new ElGamalCiphertextPojo();
    pojo.pad = ciphertext.getPad();
    pojo.data = ciphertext.getData();
    return pojo;
  }

  private static ConstantChaumPedersenProofPojo convertConstantProof(ConstantChaumPedersenProofKnownNonce proof) {
    ConstantChaumPedersenProofPojo pojo = new ConstantChaumPedersenProofPojo();
    pojo.pad = null; // LOOK
    pojo.data = null; // LOOK
    pojo.challenge = proof.getProof().getC();
    pojo.response = proof.getProof().getR();
    pojo.constant = proof.getConstant();
    return pojo;
  }

  private static DisjunctiveChaumPedersenProofPojo convertDisjunctiveProof(DisjunctiveChaumPedersenProofKnownNonce proof) {
    DisjunctiveChaumPedersenProofPojo pojo = new DisjunctiveChaumPedersenProofPojo();
    pojo.proof_zero_pad = null; // LOOK
    pojo.proof_zero_data = null; // LOOK
    pojo.proof_one_pad = null; // LOOK
    pojo.proof_one_data = null; // LOOK
    pojo.proof_zero_challenge = proof.getProof0().getC();
    pojo.proof_one_challenge = proof.getProof1().getC();
    pojo.challenge = proof.getC();
    pojo.proof_zero_response = proof.getProof0().getR();
    pojo.proof_one_response = proof.getProof1().getR();
    return pojo;
  }
}
