package electionguard.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import electionguard.core.ElementModP;
import electionguard.core.UInt256;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Conversion between ElectionContextPojo and Json, using python's object model.
 */
public class ElectionContextPojo {
  public int number_of_guardians;
  public int quorum;
  public ElementModP elgamal_public_key;
  public UInt256 manifest_hash;
  public UInt256 crypto_base_hash;
  public UInt256 crypto_extended_base_hash;
  public UInt256 commitment_hash;
  @Nullable
  public Map<String, String> extended_data;
  
  ////////////////////////////////////////////////////////////////////////////
  // deserialize

  public static ElectionContext deserialize(JsonElement jsonElem) {
    Gson gson = GsonTypeAdapters.enhancedGson();
    return new ElectionContext(gson.fromJson(jsonElem, ElectionContextPojo.class));
  }

  public static class ElectionContext {
    public final int numberOfGuardians;
    public final int quorum;
    public final ElementModP publicKey;
    public final UInt256 manifestHash;
    public final UInt256 cryptoBaseHash;
    public final UInt256 cryptoExtendedBaseHash;
    public final UInt256 commitmentHash;

    ElectionContext(ElectionContextPojo pojo) {
      this.numberOfGuardians = pojo.number_of_guardians;
      this.quorum = pojo.quorum;
      this.publicKey = pojo.elgamal_public_key;
      this.manifestHash = pojo.manifest_hash;
      this.cryptoBaseHash = pojo.crypto_base_hash;
      this.cryptoExtendedBaseHash = pojo.crypto_extended_base_hash;
      this.commitmentHash = pojo.commitment_hash;
    }
  }


}
