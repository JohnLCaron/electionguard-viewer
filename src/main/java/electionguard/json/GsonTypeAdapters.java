package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import electionguard.ballot.ElectionConstants;
import electionguard.ballot.EncryptedBallot.BallotState;
import electionguard.ballot.Guardian;
import electionguard.ballot.Manifest;
import electionguard.ballot.PlaintextBallot;
import electionguard.ballot.PlaintextTally;
import electionguard.ballot.DecryptingGuardian;
import electionguard.ballot.EncryptedBallot;
import electionguard.ballot.EncryptedTally;
import electionguard.core.ElementModP;
import electionguard.core.ElementModQ;
import electionguard.core.GroupContext;
import electionguard.core.UInt256;

import java.lang.reflect.Type;
import java.math.BigInteger;

import static electionguard.core.GroupCommonKt.safeBase16ToElementModP;
import static electionguard.core.GroupCommonKt.safeBase16ToElementModQ;
import static electionguard.core.UInt256Kt.toUInt256;
import static electionguard.viewer.KUtils.productionGroup;

/**
 * When do we need custom serializers?
 *    1. target must have no-arg constructors. But it may be private. AutoValue requires custom serializer.
 *    2. no circular references
 *    3. missing objects are set to their default (null, zero, false)
 *    4. collections must not be \<?>
 *    5. can do custom naming with @SerializedName
 */
class GsonTypeAdapters {
  private static final GroupContext group = productionGroup();

  static Gson enhancedGson() {
    return new GsonBuilder().setPrettyPrinting().serializeNulls()
            .registerTypeAdapter(BallotState.class, new BallotBoxStateSerializer())
            .registerTypeAdapter(BallotState.class, new BallotBoxStateDeserializer())
            .registerTypeAdapter(BigInteger.class, new BigIntegerDeserializer())
            .registerTypeAdapter(BigInteger.class, new BigIntegerSerializer())
            .registerTypeAdapter(Boolean.class, new BooleanSerializer())
            .registerTypeAdapter(Boolean.class, new BooleanDeserializer())
            .registerTypeAdapter(DecryptingGuardian.class, new AvailableGuardianSerializer())
            .registerTypeAdapter(DecryptingGuardian.class, new AvailableGuardianDeserializer())
            .registerTypeAdapter(EncryptedTally.class, new CiphertextTallySerializer())
            .registerTypeAdapter(EncryptedTally.class, new CiphertextTallyDeserializer())

            .registerTypeAdapter(ElectionConstants.class, new ElectionConstantsDeserializer())
            .registerTypeAdapter(ElectionContextPojo.ElectionContext.class, new ElectionContextDeserializer())
            .registerTypeAdapter(EncryptionDevicePojo.EncryptionDevice.class, new EncryptionDeviceDeserializer())

            .registerTypeAdapter(ElementModQ.class, new ModQDeserializer())
            .registerTypeAdapter(ElementModQ.class, new ModQSerializer())
            .registerTypeAdapter(ElementModP.class, new ModPDeserializer())
            .registerTypeAdapter(ElementModP.class, new ModPSerializer())
            .registerTypeAdapter(Guardian.class, new GuardianRecordSerializer())
            .registerTypeAdapter(Guardian.class, new GuardianRecordDeserializer())
            .registerTypeAdapter(UInt256.class, new UInt256Deserializer())
            .registerTypeAdapter(LagrangeCoefficientsPojo.class, new LagrangeCoefficientsSerializer())
            .registerTypeAdapter(LagrangeCoefficientsPojo.class, new LagrangeCoefficientsDeserializer())
            .registerTypeAdapter(Manifest.class, new ManifestDeserializer())
            .registerTypeAdapter(PlaintextBallot.class, new PlaintextBallotSerializer())
            .registerTypeAdapter(PlaintextBallot.class, new PlaintextBallotDeserializer())
            .registerTypeAdapter(PlaintextTally.class, new PlaintextTallySerializer())
            .registerTypeAdapter(PlaintextTally.class, new PlaintextTallyDeserializer())
            .registerTypeAdapter(EncryptedBallot.class, new CiphertextBallotSerializer())
            .registerTypeAdapter(EncryptedBallot.class, new CiphertextBallotDeserializer())
            .create();
  }

  private static class AvailableGuardianDeserializer implements JsonDeserializer<DecryptingGuardian> {
    @Override
    public DecryptingGuardian deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return DecryptingGuardianPojo.deserialize(json);
    }
  }

  private static class AvailableGuardianSerializer implements JsonSerializer<DecryptingGuardian> {
    @Override
    public JsonElement serialize(DecryptingGuardian src, Type typeOfSrc, JsonSerializationContext context) {
      return DecryptingGuardianPojo.serialize(src);
    }
  }

  // ?? JsonElement.getAsBigInteger()
  private static class BigIntegerDeserializer implements JsonDeserializer<BigInteger> {
    @Override
    public BigInteger deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return new BigInteger(content, 16);
    }
  }

  private static class BigIntegerSerializer implements JsonSerializer<BigInteger> {
    @Override
    public JsonElement serialize(BigInteger src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.toString(16));
    }
  }

  private static class UInt256Deserializer implements JsonDeserializer<UInt256> {
    @Override
    public UInt256 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return toUInt256(safeBase16ToElementModQ(group, content));
    }
  }

  private static class ModQDeserializer implements JsonDeserializer<ElementModQ> {
    @Override
    public ElementModQ deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return safeBase16ToElementModQ(group, content);
    }
  }

  private static class ModQSerializer implements JsonSerializer<ElementModQ> {
    @Override
    public JsonElement serialize(ElementModQ src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.cryptoHashString());
    }
  }

  private static class ModPDeserializer implements JsonDeserializer<ElementModP> {
    @Override
    public ElementModP deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return safeBase16ToElementModP(group, content);
    }
  }

  private static class ModPSerializer implements JsonSerializer<ElementModP> {
    @Override
    public JsonElement serialize(ElementModP src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.cryptoHashString());
    }
  }

  private static class ManifestDeserializer implements JsonDeserializer<Manifest> {
    @Override
    public Manifest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return ManifestPojo.deserialize(json);
    }
  }

  private static class CiphertextBallotSerializer implements JsonSerializer<EncryptedBallot> {
    @Override
    public JsonElement serialize(EncryptedBallot src, Type typeOfSrc, JsonSerializationContext context) {
      return EncryptedBallotPojo.serialize(src);
    }
  }

  private static class CiphertextBallotDeserializer implements JsonDeserializer<EncryptedBallot> {
    @Override
    public EncryptedBallot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return EncryptedBallotPojo.deserialize(json);
    }
  }

  private static class PlaintextBallotSerializer implements JsonSerializer<PlaintextBallot> {
    @Override
    public JsonElement serialize(PlaintextBallot src, Type typeOfSrc, JsonSerializationContext context) {
      return PlaintextBallotPojo.serialize(src);
    }
  }

  private static class PlaintextBallotDeserializer implements JsonDeserializer<PlaintextBallot> {
    @Override
    public PlaintextBallot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return PlaintextBallotPojo.deserialize(json);
    }
  }

  private static class PlaintextTallySerializer implements JsonSerializer<PlaintextTally> {
    @Override
    public JsonElement serialize(PlaintextTally src, Type typeOfSrc, JsonSerializationContext context) {
      return PlaintextTallyPojo.serialize(src);
    }
  }

  private static class PlaintextTallyDeserializer implements JsonDeserializer<PlaintextTally> {
    @Override
    public PlaintextTally deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return PlaintextTallyPojo.deserialize(json);
    }
  }

  private static class CiphertextTallySerializer implements JsonSerializer<EncryptedTally> {
    @Override
    public JsonElement serialize(EncryptedTally src, Type typeOfSrc, JsonSerializationContext context) {
      return EncryptedTallyPojo.serialize(src);
    }
  }

  private static class CiphertextTallyDeserializer implements JsonDeserializer<EncryptedTally> {
    @Override
    public EncryptedTally deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return EncryptedTallyPojo.deserialize(json);
    }
  }

  private static class ElectionConstantsDeserializer implements JsonDeserializer<ElectionConstants> {
    @Override
    public ElectionConstants deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return ElectionConstantsPojo.deserialize(json);
    }
  }

  private static class ElectionContextDeserializer implements JsonDeserializer<ElectionContextPojo.ElectionContext> {
    @Override
    public ElectionContextPojo.ElectionContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return ElectionContextPojo.deserialize(json);
    }
  }

  private static class EncryptionDeviceDeserializer implements JsonDeserializer<EncryptionDevicePojo.EncryptionDevice> {
    @Override
    public EncryptionDevicePojo.EncryptionDevice deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return EncryptionDevicePojo.deserialize(json);
    }
  }

  private static class GuardianRecordSerializer implements JsonSerializer<Guardian> {
    @Override
    public JsonElement serialize(Guardian src, Type typeOfSrc, JsonSerializationContext context) {
      return GuardianRecordPojo.serialize(src);
    }
  }

  private static class GuardianRecordDeserializer implements JsonDeserializer<Guardian> {
    @Override
    public Guardian deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return GuardianRecordPojo.deserialize(json);
    }
  }

  private static class LagrangeCoefficientsSerializer implements JsonSerializer<LagrangeCoefficientsPojo> {
    @Override
    public JsonElement serialize(LagrangeCoefficientsPojo src, Type typeOfSrc, JsonSerializationContext context) {
      return LagrangeCoefficientsPojo.serialize(src);
    }
  }

  private static class LagrangeCoefficientsDeserializer implements JsonDeserializer<LagrangeCoefficientsPojo> {
    @Override
    public LagrangeCoefficientsPojo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      return LagrangeCoefficientsPojo.deserialize(json);
    }
  }

  private static class IntegerSerializer implements JsonSerializer<Integer> {
    @Override
    public JsonElement serialize(Integer src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(Integer.toHexString(src));
    }
  }

  private static class IntegerDeserializer implements JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return Integer.parseInt(content, 16); // LOOK should it be unsigned?
    }
  }

  private static class LongSerializer implements JsonSerializer<Long> {
    @Override
    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(Long.toHexString(src));
    }
  }

  private static class LongDeserializer implements JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      return Long.parseUnsignedLong(content, 16);
    }
  }

  private static class BooleanSerializer implements JsonSerializer<Boolean> {
    @Override
    public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src ? "01" : "00");
    }
  }

  private static class BooleanDeserializer implements JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      String content = json.getAsJsonPrimitive().getAsString();
      switch (content) {
        case "00": return false;
        case "false": return false;
        case "01": return true;
        case "true": return true;
      }
      throw new IllegalStateException("Unknown boolean encoding " + content);
    }
  }

  private static class BallotBoxStateSerializer implements JsonSerializer<BallotState> {
    @Override
    public JsonElement serialize(BallotState state, Type typeOfSrc, JsonSerializationContext context) {
      int content = 3;
      switch (state) {
        case CAST: content =  1; break;
        case SPOILED: content =  2; break;
      }
      return new JsonPrimitive(content);
    }
  }

  private static class BallotBoxStateDeserializer implements JsonDeserializer<BallotState> {
    @Override
    public BallotState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
      int content = json.getAsJsonPrimitive().getAsNumber().intValue();
      switch (content) {
        case 1: return BallotState.CAST;
        case 2: return BallotState.SPOILED;
        case 3: return BallotState.UNKNOWN;
      }
      throw new IllegalStateException("Unknown BallotState encoding " + content);
    }
  }
}
